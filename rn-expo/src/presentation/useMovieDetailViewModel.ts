import { useReducer, useCallback } from "react";
import { MovieDetail } from "../domain/entities";
import { GetMovieDetailUseCase } from "../application/GetMovieDetailUseCase";

export interface MovieDetailUiState {
  isLoading: boolean;
  movieDetail: MovieDetail | null;
  error: string | null;
}

export const initialState: MovieDetailUiState = {
  isLoading: false,
  movieDetail: null,
  error: null,
};

type Action =
  | { type: "LOAD_START" }
  | { type: "LOAD_SUCCESS"; payload: MovieDetail }
  | { type: "LOAD_FAILURE"; error: string };

export function reducer(
  state: MovieDetailUiState,
  action: Action
): MovieDetailUiState {
  switch (action.type) {
    case "LOAD_START":
      return { isLoading: true, movieDetail: null, error: null };
    case "LOAD_SUCCESS":
      return { isLoading: false, movieDetail: action.payload, error: null };
    case "LOAD_FAILURE":
      return { isLoading: false, movieDetail: null, error: action.error };
    default:
      return state;
  }
}

export function useMovieDetailViewModel(
  useCase: GetMovieDetailUseCase,
  movieId: number
) {
  const [state, dispatch] = useReducer(reducer, initialState);

  const loadDetail = useCallback(async () => {
    dispatch({ type: "LOAD_START" });
    const result = await useCase.execute(movieId);
    if (result.success) {
      dispatch({ type: "LOAD_SUCCESS", payload: result.data });
    } else {
      dispatch({ type: "LOAD_FAILURE", error: result.error });
    }
  }, [useCase, movieId]);

  const retry = useCallback(async () => {
    await loadDetail();
  }, [loadDetail]);

  return { state, loadDetail, retry };
}
