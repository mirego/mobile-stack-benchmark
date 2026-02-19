import { MovieDetail } from "../domain/entities";
import { MovieRepository, Result } from "../domain/MovieRepository";

export class GetMovieDetailUseCase {
  constructor(private readonly repository: MovieRepository) {}

  async execute(id: number): Promise<Result<MovieDetail>> {
    return this.repository.getMovieDetail(id);
  }
}
