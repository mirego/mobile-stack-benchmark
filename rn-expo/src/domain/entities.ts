export interface Movie {
  id: number;
  title: string;
  overview: string;
  posterPath: string | null;
  backdropPath: string | null;
  releaseDate: string;
  voteAverage: number;
  voteCount: number;
}

export interface Genre {
  id: number;
  name: string;
}

export interface MovieDetail extends Movie {
  tagline: string | null;
  runtime: number | null;
  genres: Genre[];
}

export interface MoviePage {
  movies: Movie[];
  page: number;
  totalPages: number;
}
