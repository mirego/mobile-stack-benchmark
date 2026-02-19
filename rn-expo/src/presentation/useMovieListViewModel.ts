import { useReducer, useCallback, useRef } from "react";
import { Movie, MoviePage } from "../domain/entities";
import { GetPopularMoviesUseCase } from "../application/GetPopularMoviesUseCase";

export interface MovieListUiState {
  isLoading: boolean;
  movies: Movie[];
  error: string | null;
  currentPage: number;
  totalPages: number;
  isLoadingMore: boolean;
}

export const initialState: MovieListUiState = {
  isLoading: false,
  movies: [],
  error: null,
  currentPage: 0,
  totalPages: Number.MAX_SAFE_INTEGER,
  isLoadingMore: false,
};

type Action =
  | { type: "LOAD_FIRST_PAGE_START" }
  | { type: "LOAD_FIRST_PAGE_SUCCESS"; payload: MoviePage }
  | { type: "LOAD_FIRST_PAGE_FAILURE"; error: string }
  | { type: "LOAD_NEXT_PAGE_START" }
  | { type: "LOAD_NEXT_PAGE_SUCCESS"; payload: MoviePage }
  | { type: "LOAD_NEXT_PAGE_FAILURE"; error: string };

export function reducer(
  state: MovieListUiState,
  action: Action
): MovieListUiState {
  switch (action.type) {
    case "LOAD_FIRST_PAGE_START":
      return { ...initialState, isLoading: true };
    case "LOAD_FIRST_PAGE_SUCCESS":
      return {
        ...state,
        isLoading: false,
        movies: action.payload.movies,
        currentPage: action.payload.page,
        totalPages: action.payload.totalPages,
        error: null,
      };
    case "LOAD_FIRST_PAGE_FAILURE":
      return {
        ...state,
        isLoading: false,
        error: action.error,
      };
    case "LOAD_NEXT_PAGE_START":
      return { ...state, isLoadingMore: true, error: null };
    case "LOAD_NEXT_PAGE_SUCCESS":
      return {
        ...state,
        isLoadingMore: false,
        movies: [...state.movies, ...action.payload.movies],
        currentPage: action.payload.page,
        totalPages: action.payload.totalPages,
      };
    case "LOAD_NEXT_PAGE_FAILURE":
      return {
        ...state,
        isLoadingMore: false,
        error: action.error,
      };
    default:
      return state;
  }
}

type LastAction = "loadFirstPage" | "loadNextPage";

export function useMovieListViewModel(useCase: GetPopularMoviesUseCase) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const lastActionRef = useRef<LastAction | null>(null);
  const stateRef = useRef(state);
  stateRef.current = state;

  const loadFirstPage = useCallback(async () => {
    lastActionRef.current = "loadFirstPage";
    dispatch({ type: "LOAD_FIRST_PAGE_START" });
    const result = await useCase.execute(1);
    if (result.success) {
      dispatch({ type: "LOAD_FIRST_PAGE_SUCCESS", payload: result.data });
    } else {
      dispatch({ type: "LOAD_FIRST_PAGE_FAILURE", error: result.error });
    }
  }, [useCase]);

  const loadNextPage = useCallback(async () => {
    const s = stateRef.current;
    if (s.isLoading || s.isLoadingMore) return;
    if (s.currentPage >= s.totalPages) return;

    lastActionRef.current = "loadNextPage";
    dispatch({ type: "LOAD_NEXT_PAGE_START" });
    const result = await useCase.execute(s.currentPage + 1);
    if (result.success) {
      dispatch({ type: "LOAD_NEXT_PAGE_SUCCESS", payload: result.data });
    } else {
      dispatch({ type: "LOAD_NEXT_PAGE_FAILURE", error: result.error });
    }
  }, [useCase]);

  const retry = useCallback(async () => {
    if (stateRef.current.error === null) return;
    if (lastActionRef.current === "loadFirstPage") {
      await loadFirstPage();
    } else if (lastActionRef.current === "loadNextPage") {
      await loadNextPage();
    }
  }, [loadFirstPage, loadNextPage]);

  return { state, loadFirstPage, loadNextPage, retry };
}
