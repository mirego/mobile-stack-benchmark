export interface MovieDto {
  id: number;
  title: string;
  overview: string;
  poster_path: string | null;
  backdrop_path: string | null;
  release_date: string;
  vote_average: number;
  vote_count: number;
}

export interface GenreDto {
  id: number;
  name: string;
}

export interface MovieDetailDto extends MovieDto {
  tagline: string | null;
  runtime: number | null;
  genres: GenreDto[];
}

export interface PopularMoviesResponseDto {
  page: number;
  total_pages: number;
  results: MovieDto[];
}
