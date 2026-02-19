import { GetMovieDetailUseCase } from "../../src/application/GetMovieDetailUseCase";
import { MovieRepository } from "../../src/domain/MovieRepository";
import { MovieDetail } from "../../src/domain/entities";

const mockMovieDetail: MovieDetail = {
  id: 1,
  title: "Test Movie",
  overview: "Test overview",
  posterPath: "/test.jpg",
  backdropPath: "/backdrop.jpg",
  releaseDate: "2024-01-15",
  voteAverage: 7.8,
  voteCount: 1200,
  tagline: "Test tagline",
  runtime: 142,
  genres: [{ id: 28, name: "Action" }],
};

describe("GetMovieDetailUseCase", () => {
  it("returns success when repository returns data", async () => {
    const mockRepo: MovieRepository = {
      getPopularMovies: jest.fn(),
      getMovieDetail: jest
        .fn()
        .mockResolvedValue({ success: true, data: mockMovieDetail }),
    };
    const useCase = new GetMovieDetailUseCase(mockRepo);

    const result = await useCase.execute(1);

    expect(result).toEqual({ success: true, data: mockMovieDetail });
    expect(mockRepo.getMovieDetail).toHaveBeenCalledWith(1);
  });

  it("returns failure when repository fails", async () => {
    const mockRepo: MovieRepository = {
      getPopularMovies: jest.fn(),
      getMovieDetail: jest
        .fn()
        .mockResolvedValue({ success: false, error: "Not found" }),
    };
    const useCase = new GetMovieDetailUseCase(mockRepo);

    const result = await useCase.execute(1);

    expect(result).toEqual({ success: false, error: "Not found" });
  });
});
