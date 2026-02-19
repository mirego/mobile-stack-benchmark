import axios, { AxiosInstance } from "axios";
import { MovieDetailDto, PopularMoviesResponseDto } from "./dtos";

export class TmdbApiClient {
  private client: AxiosInstance;

  constructor(baseUrl: string, apiKey: string) {
    this.client = axios.create({
      baseURL: baseUrl,
      params: { api_key: apiKey },
    });
  }

  async getPopularMovies(page: number): Promise<PopularMoviesResponseDto> {
    const response = await this.client.get<PopularMoviesResponseDto>(
      "/movie/popular",
      { params: { page } }
    );
    return response.data;
  }

  async getMovieDetail(id: number): Promise<MovieDetailDto> {
    const response = await this.client.get<MovieDetailDto>(`/movie/${id}`);
    return response.data;
  }
}
