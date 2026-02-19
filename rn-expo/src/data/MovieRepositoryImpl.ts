import { MovieDetail, MoviePage } from "../domain/entities";
import { MovieRepository, Result } from "../domain/MovieRepository";
import {
  mapMovieDetailDtoToDomain,
  mapPopularMoviesResponseToDomain,
} from "./mappers";
import { TmdbApiClient } from "./TmdbApiClient";

export class MovieRepositoryImpl implements MovieRepository {
  constructor(private readonly apiClient: TmdbApiClient) {}

  async getPopularMovies(page: number): Promise<Result<MoviePage>> {
    try {
      const dto = await this.apiClient.getPopularMovies(page);
      return { success: true, data: mapPopularMoviesResponseToDomain(dto) };
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "An unknown error occurred";
      return { success: false, error: message };
    }
  }

  async getMovieDetail(id: number): Promise<Result<MovieDetail>> {
    try {
      const dto = await this.apiClient.getMovieDetail(id);
      return { success: true, data: mapMovieDetailDtoToDomain(dto) };
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "An unknown error occurred";
      return { success: false, error: message };
    }
  }
}
