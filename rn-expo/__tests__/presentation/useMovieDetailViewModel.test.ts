import { renderHook, act } from "@testing-library/react-native";
import { useMovieDetailViewModel } from "../../src/presentation/useMovieDetailViewModel";
import { GetMovieDetailUseCase } from "../../src/application/GetMovieDetailUseCase";
import { MovieRepository } from "../../src/domain/MovieRepository";
import { MovieDetail } from "../../src/domain/entities";

const movieDetail: MovieDetail = {
  id: 42,
  title: "Test Movie",
  overview: "A great movie about testing.",
  posterPath: "/poster42.jpg",
  backdropPath: "/backdrop42.jpg",
  releaseDate: "2024-06-15",
  voteAverage: 8.2,
  voteCount: 3400,
  tagline: "Testing is believing",
  runtime: 142,
  genres: [
    { id: 28, name: "Action" },
    { id: 12, name: "Adventure" },
  ],
};

function createMockUseCase(mockFn: jest.Mock): GetMovieDetailUseCase {
  const mockRepo: MovieRepository = {
    getPopularMovies: jest.fn(),
    getMovieDetail: mockFn,
  };
  return new GetMovieDetailUseCase(mockRepo);
}

describe("useMovieDetailViewModel", () => {
  it("has correct initial state", () => {
    const useCase = createMockUseCase(jest.fn());
    const { result } = renderHook(() =>
      useMovieDetailViewModel(useCase, 42)
    );

    expect(result.current.state).toEqual({
      isLoading: false,
      movieDetail: null,
      error: null,
    });
  });

  it("loadDetail sets movieDetail on success", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValue({ success: true, data: movieDetail });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() =>
      useMovieDetailViewModel(useCase, 42)
    );

    await act(async () => {
      await result.current.loadDetail();
    });

    expect(result.current.state.isLoading).toBe(false);
    expect(result.current.state.movieDetail).toEqual(movieDetail);
    expect(result.current.state.error).toBeNull();
    expect(mockFn).toHaveBeenCalledWith(42);
  });

  it("loadDetail sets error on failure", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValue({ success: false, error: "Not found" });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() =>
      useMovieDetailViewModel(useCase, 42)
    );

    await act(async () => {
      await result.current.loadDetail();
    });

    expect(result.current.state.isLoading).toBe(false);
    expect(result.current.state.movieDetail).toBeNull();
    expect(result.current.state.error).toBe("Not found");
  });

  it("retry after failure re-triggers loadDetail", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: false, error: "Network error" })
      .mockResolvedValueOnce({ success: true, data: movieDetail });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() =>
      useMovieDetailViewModel(useCase, 42)
    );

    await act(async () => {
      await result.current.loadDetail();
    });

    expect(result.current.state.error).toBe("Network error");

    await act(async () => {
      await result.current.retry();
    });

    expect(result.current.state.movieDetail).toEqual(movieDetail);
    expect(result.current.state.error).toBeNull();
    expect(mockFn).toHaveBeenCalledTimes(2);
  });
});
