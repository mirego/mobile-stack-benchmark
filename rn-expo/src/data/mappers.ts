import { Genre, Movie, MovieDetail, MoviePage } from "../domain/entities";
import {
  GenreDto,
  MovieDetailDto,
  MovieDto,
  PopularMoviesResponseDto,
} from "./dtos";

export function mapMovieDtoToDomain(dto: MovieDto): Movie {
  return {
    id: dto.id,
    title: dto.title,
    overview: dto.overview,
    posterPath: dto.poster_path,
    backdropPath: dto.backdrop_path,
    releaseDate: dto.release_date,
    voteAverage: dto.vote_average,
    voteCount: dto.vote_count,
  };
}

export function mapGenreDtoToDomain(dto: GenreDto): Genre {
  return {
    id: dto.id,
    name: dto.name,
  };
}

export function mapMovieDetailDtoToDomain(dto: MovieDetailDto): MovieDetail {
  return {
    ...mapMovieDtoToDomain(dto),
    tagline: dto.tagline,
    runtime: dto.runtime,
    genres: dto.genres.map(mapGenreDtoToDomain),
  };
}

export function mapPopularMoviesResponseToDomain(
  dto: PopularMoviesResponseDto
): MoviePage {
  return {
    movies: dto.results.map(mapMovieDtoToDomain),
    page: dto.page,
    totalPages: dto.total_pages,
  };
}
