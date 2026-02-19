import { useEffect } from "react";
import {
  View,
  Text,
  FlatList,
  ActivityIndicator,
  StyleSheet,
  Pressable,
} from "react-native";
import { useRouter } from "expo-router";
import { Movie } from "../src/domain/entities";
import { useMovieListViewModel } from "../src/presentation/useMovieListViewModel";
import { MovieListItem } from "../src/components/MovieListItem";
import { getPopularMoviesUseCase } from "../src/di";

export default function MovieListScreen() {
  const router = useRouter();
  const { state, loadFirstPage, loadNextPage, retry } =
    useMovieListViewModel(getPopularMoviesUseCase);

  useEffect(() => {
    loadFirstPage();
  }, [loadFirstPage]);

  const handleMoviePress = (movie: Movie) => {
    router.push(`/movie/${movie.id}`);
  };

  if (state.isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (state.error && state.movies.length === 0) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>{state.error}</Text>
        <Pressable style={styles.retryButton} onPress={retry}>
          <Text style={styles.retryText}>Retry</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <FlatList
      data={state.movies}
      keyExtractor={(_, index) => index.toString()}
      renderItem={({ item }) => (
        <MovieListItem movie={item} onPress={handleMoviePress} />
      )}
      onEndReached={loadNextPage}
      onEndReachedThreshold={0.5}
      ListFooterComponent={
        state.isLoadingMore ? (
          <ActivityIndicator style={styles.footer} size="small" />
        ) : state.error ? (
          <View style={styles.footerError}>
            <Text style={styles.errorText}>{state.error}</Text>
            <Pressable style={styles.retryButton} onPress={retry}>
              <Text style={styles.retryText}>Retry</Text>
            </Pressable>
          </View>
        ) : null
      }
    />
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 16,
  },
  errorText: {
    fontSize: 16,
    color: "#cc0000",
    textAlign: "center",
    marginBottom: 12,
  },
  retryButton: {
    paddingHorizontal: 24,
    paddingVertical: 10,
    backgroundColor: "#007AFF",
    borderRadius: 8,
  },
  retryText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
  footer: {
    padding: 16,
  },
  footerError: {
    padding: 16,
    alignItems: "center",
  },
});
