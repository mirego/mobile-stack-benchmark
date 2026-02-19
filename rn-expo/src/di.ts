import { TmdbApiClient } from "./data/TmdbApiClient";
import { MovieRepositoryImpl } from "./data/MovieRepositoryImpl";
import { GetPopularMoviesUseCase } from "./application/GetPopularMoviesUseCase";
import { GetMovieDetailUseCase } from "./application/GetMovieDetailUseCase";
import { TMDB_API_KEY, TMDB_BASE_URL } from "./config";

const apiClient = new TmdbApiClient(TMDB_BASE_URL, TMDB_API_KEY);
const movieRepository = new MovieRepositoryImpl(apiClient);

export const getPopularMoviesUseCase = new GetPopularMoviesUseCase(
  movieRepository
);
export const getMovieDetailUseCase = new GetMovieDetailUseCase(
  movieRepository
);
