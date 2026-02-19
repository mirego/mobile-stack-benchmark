import { useEffect } from "react";
import {
  View,
  Text,
  Image,
  ScrollView,
  ActivityIndicator,
  Pressable,
  StyleSheet,
} from "react-native";
import { useLocalSearchParams } from "expo-router";
import { useMovieDetailViewModel } from "../../src/presentation/useMovieDetailViewModel";
import { getMovieDetailUseCase } from "../../src/di";

export default function MovieDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const movieId = Number(id);
  const { state, loadDetail, retry } = useMovieDetailViewModel(
    getMovieDetailUseCase,
    movieId
  );

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  if (state.isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (state.error) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>{state.error}</Text>
        <Pressable style={styles.retryButton} onPress={retry}>
          <Text style={styles.retryText}>Retry</Text>
        </Pressable>
      </View>
    );
  }

  const movie = state.movieDetail;
  if (!movie) return null;

  const backdropUri = movie.backdropPath
    ? `https://image.tmdb.org/t/p/w780${movie.backdropPath}`
    : null;
  const posterUri = movie.posterPath
    ? `https://image.tmdb.org/t/p/w342${movie.posterPath}`
    : null;

  return (
    <ScrollView style={styles.container}>
      <View style={styles.headerContainer}>
        {backdropUri ? (
          <Image source={{ uri: backdropUri }} style={styles.backdrop} />
        ) : (
          <View style={[styles.backdrop, styles.backdropPlaceholder]} />
        )}
        {posterUri && (
          <Image source={{ uri: posterUri }} style={styles.poster} />
        )}
      </View>

      <View style={styles.content}>
        <Text style={styles.title}>{movie.title}</Text>
        {movie.tagline ? (
          <Text style={styles.tagline}>{movie.tagline}</Text>
        ) : null}

        <Text style={styles.rating}>★ {movie.voteAverage.toFixed(1)}</Text>

        {movie.genres.length > 0 && (
          <View style={styles.genresRow}>
            {movie.genres.map((genre) => (
              <View key={genre.id} style={styles.genreChip}>
                <Text style={styles.genreText}>{genre.name}</Text>
              </View>
            ))}
          </View>
        )}

        <View style={styles.metaRow}>
          <Text style={styles.metaText}>
            {movie.runtime ? `${movie.runtime} min` : "N/A"}
          </Text>
          <Text style={styles.metaText}>{movie.releaseDate}</Text>
        </View>

        <Text style={styles.overview}>{movie.overview}</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
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
  headerContainer: {
    position: "relative",
    marginBottom: 60,
  },
  backdrop: {
    width: "100%",
    height: 220,
  },
  backdropPlaceholder: {
    backgroundColor: "#e0e0e0",
  },
  poster: {
    position: "absolute",
    bottom: -50,
    left: 16,
    width: 120,
    height: 180,
    borderRadius: 8,
    borderWidth: 2,
    borderColor: "#fff",
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    marginBottom: 4,
  },
  tagline: {
    fontSize: 15,
    fontStyle: "italic",
    color: "#666",
    marginBottom: 8,
  },
  rating: {
    fontSize: 16,
    color: "#f5a623",
    fontWeight: "600",
    marginBottom: 12,
  },
  genresRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    marginBottom: 12,
    gap: 8,
  },
  genreChip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: "#f0f0f0",
    borderRadius: 16,
  },
  genreText: {
    fontSize: 13,
    color: "#333",
  },
  metaRow: {
    flexDirection: "row",
    gap: 16,
    marginBottom: 16,
  },
  metaText: {
    fontSize: 14,
    color: "#888",
  },
  overview: {
    fontSize: 15,
    lineHeight: 22,
    color: "#333",
  },
});
