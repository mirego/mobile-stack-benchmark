import { GetPopularMoviesUseCase } from "../../src/application/GetPopularMoviesUseCase";
import { MovieRepository } from "../../src/domain/MovieRepository";
import { MoviePage } from "../../src/domain/entities";

const mockMoviePage: MoviePage = {
  movies: [
    {
      id: 1,
      title: "Test Movie",
      overview: "Test overview",
      posterPath: "/test.jpg",
      backdropPath: "/backdrop.jpg",
      releaseDate: "2024-01-15",
      voteAverage: 7.8,
      voteCount: 1200,
    },
  ],
  page: 1,
  totalPages: 10,
};

describe("GetPopularMoviesUseCase", () => {
  it("returns success when repository returns data", async () => {
    const mockRepo: MovieRepository = {
      getPopularMovies: jest
        .fn()
        .mockResolvedValue({ success: true, data: mockMoviePage }),
      getMovieDetail: jest.fn(),
    };
    const useCase = new GetPopularMoviesUseCase(mockRepo);

    const result = await useCase.execute(1);

    expect(result).toEqual({ success: true, data: mockMoviePage });
    expect(mockRepo.getPopularMovies).toHaveBeenCalledWith(1);
  });

  it("returns failure when repository fails", async () => {
    const mockRepo: MovieRepository = {
      getPopularMovies: jest
        .fn()
        .mockResolvedValue({ success: false, error: "Network error" }),
      getMovieDetail: jest.fn(),
    };
    const useCase = new GetPopularMoviesUseCase(mockRepo);

    const result = await useCase.execute(1);

    expect(result).toEqual({ success: false, error: "Network error" });
  });
});
