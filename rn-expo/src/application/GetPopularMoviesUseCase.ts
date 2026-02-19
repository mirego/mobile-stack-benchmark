import { MoviePage } from "../domain/entities";
import { MovieRepository, Result } from "../domain/MovieRepository";

export class GetPopularMoviesUseCase {
  constructor(private readonly repository: MovieRepository) {}

  async execute(page: number): Promise<Result<MoviePage>> {
    return this.repository.getPopularMovies(page);
  }
}
