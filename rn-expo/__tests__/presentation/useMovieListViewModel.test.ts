import { renderHook, act } from "@testing-library/react-native";
import { useMovieListViewModel } from "../../src/presentation/useMovieListViewModel";
import { GetPopularMoviesUseCase } from "../../src/application/GetPopularMoviesUseCase";
import { MovieRepository } from "../../src/domain/MovieRepository";
import { MoviePage } from "../../src/domain/entities";

const page1: MoviePage = {
  movies: [
    {
      id: 1,
      title: "Movie 1",
      overview: "Overview 1",
      posterPath: "/poster1.jpg",
      backdropPath: "/backdrop1.jpg",
      releaseDate: "2024-01-15",
      voteAverage: 7.8,
      voteCount: 1200,
    },
  ],
  page: 1,
  totalPages: 3,
};

const page2: MoviePage = {
  movies: [
    {
      id: 2,
      title: "Movie 2",
      overview: "Overview 2",
      posterPath: "/poster2.jpg",
      backdropPath: "/backdrop2.jpg",
      releaseDate: "2024-02-20",
      voteAverage: 6.5,
      voteCount: 800,
    },
  ],
  page: 2,
  totalPages: 3,
};

function createMockUseCase(
  mockFn: jest.Mock
): GetPopularMoviesUseCase {
  const mockRepo: MovieRepository = {
    getPopularMovies: mockFn,
    getMovieDetail: jest.fn(),
  };
  return new GetPopularMoviesUseCase(mockRepo);
}

describe("useMovieListViewModel", () => {
  it("has correct initial state", () => {
    const useCase = createMockUseCase(jest.fn());
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    expect(result.current.state).toEqual({
      isLoading: false,
      movies: [],
      error: null,
      currentPage: 0,
      totalPages: Number.MAX_SAFE_INTEGER,
      isLoadingMore: false,
    });
  });

  it("loadFirstPage sets movies on success", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValue({ success: true, data: page1 });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    expect(result.current.state.isLoading).toBe(false);
    expect(result.current.state.movies).toEqual(page1.movies);
    expect(result.current.state.currentPage).toBe(1);
    expect(result.current.state.totalPages).toBe(3);
    expect(result.current.state.error).toBeNull();
  });

  it("loadFirstPage sets error on failure", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValue({ success: false, error: "Network error" });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    expect(result.current.state.isLoading).toBe(false);
    expect(result.current.state.movies).toEqual([]);
    expect(result.current.state.error).toBe("Network error");
  });

  it("loadNextPage appends movies", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: true, data: page1 })
      .mockResolvedValueOnce({ success: true, data: page2 });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    await act(async () => {
      await result.current.loadNextPage();
    });

    expect(result.current.state.movies).toEqual([
      ...page1.movies,
      ...page2.movies,
    ]);
    expect(result.current.state.currentPage).toBe(2);
  });

  it("loadNextPage is a no-op when at last page", async () => {
    const lastPage: MoviePage = { ...page1, page: 3, totalPages: 3 };
    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: true, data: lastPage });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    mockFn.mockClear();

    await act(async () => {
      await result.current.loadNextPage();
    });

    expect(mockFn).not.toHaveBeenCalled();
  });

  it("retry after failure re-triggers load", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: false, error: "Network error" })
      .mockResolvedValueOnce({ success: true, data: page1 });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    expect(result.current.state.error).toBe("Network error");

    await act(async () => {
      await result.current.retry();
    });

    expect(result.current.state.movies).toEqual(page1.movies);
    expect(result.current.state.error).toBeNull();
  });

  it("retry after success is a no-op", async () => {
    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: true, data: page1 });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    mockFn.mockClear();

    await act(async () => {
      await result.current.retry();
    });

    expect(mockFn).not.toHaveBeenCalled();
  });

  it("loadFirstPage resets state when called with existing movies", async () => {
    const freshPage: MoviePage = {
      movies: [
        {
          id: 99,
          title: "Fresh Movie",
          overview: "Fresh overview",
          posterPath: "/fresh.jpg",
          backdropPath: "/fresh_bg.jpg",
          releaseDate: "2025-06-01",
          voteAverage: 9.0,
          voteCount: 2000,
        },
      ],
      page: 1,
      totalPages: 5,
    };

    const mockFn = jest
      .fn()
      .mockResolvedValueOnce({ success: true, data: page1 })
      .mockResolvedValueOnce({ success: true, data: freshPage });
    const useCase = createMockUseCase(mockFn);
    const { result } = renderHook(() => useMovieListViewModel(useCase));

    await act(async () => {
      await result.current.loadFirstPage();
    });

    expect(result.current.state.movies).toEqual(page1.movies);

    await act(async () => {
      await result.current.loadFirstPage();
    });

    expect(result.current.state.movies).toEqual(freshPage.movies);
    expect(result.current.state.currentPage).toBe(1);
    expect(result.current.state.totalPages).toBe(5);
  });
});
