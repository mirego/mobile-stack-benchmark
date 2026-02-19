import { MovieDetail, MoviePage } from "./entities";

export type Result<T> =
  | { success: true; data: T }
  | { success: false; error: string };

export interface MovieRepository {
  getPopularMovies(page: number): Promise<Result<MoviePage>>;
  getMovieDetail(id: number): Promise<Result<MovieDetail>>;
}
