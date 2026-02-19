import React from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import { Image } from "expo-image";
import { Movie } from "../domain/entities";
import { TMDB_IMAGE_BASE_URL } from "../config";

interface MovieListItemProps {
  movie: Movie;
  onPress: (movie: Movie) => void;
}

function getStarRating(voteAverage: number): string {
  const filled = Math.round(voteAverage / 2);
  return "★".repeat(filled) + "☆".repeat(5 - filled);
}

function formatVoteCount(count: number): string {
  return count.toLocaleString();
}

function getPillColor(voteAverage: number): string {
  if (voteAverage >= 7) return "#4CAF50";
  if (voteAverage >= 5) return "#FF9800";
  return "#F44336";
}

export function MovieListItem({ movie, onPress }: MovieListItemProps) {
  const year = movie.releaseDate ? movie.releaseDate.substring(0, 4) : "N/A";
  const posterUri = movie.posterPath
    ? `${TMDB_IMAGE_BASE_URL}/w342${movie.posterPath}`
    : null;

  return (
    <Pressable style={styles.container} onPress={() => onPress(movie)}>
      {posterUri ? (
        <Image
          source={{ uri: posterUri }}
          style={styles.poster}
          contentFit="cover"
        />
      ) : (
        <View style={[styles.poster, styles.posterPlaceholder]}>
          <Text style={styles.placeholderText}>No Image</Text>
        </View>
      )}
      <View style={styles.info}>
        <Text style={styles.title} numberOfLines={2}>
          {movie.title}
        </Text>
        <Text style={styles.year}>{year}</Text>
        <Text style={styles.starRating}>
          {getStarRating(movie.voteAverage)}{" "}
          <Text style={styles.starNumeric}>{movie.voteAverage.toFixed(1)}</Text>
        </Text>
        <Text style={styles.voteCount}>
          {formatVoteCount(movie.voteCount)} votes
        </Text>
        <Text style={styles.overview} numberOfLines={3}>
          {movie.overview}
        </Text>
        <View
          style={[
            styles.pill,
            { backgroundColor: getPillColor(movie.voteAverage) },
          ]}
        >
          <Text style={styles.pillText}>{movie.voteAverage.toFixed(1)}</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    padding: 12,
    alignItems: "flex-start",
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e0e0e0",
  },
  poster: {
    width: 80,
    height: 120,
    borderRadius: 8,
    backgroundColor: "#e0e0e0",
  },
  posterPlaceholder: {
    justifyContent: "center",
    alignItems: "center",
  },
  placeholderText: {
    fontSize: 10,
    color: "#999",
  },
  info: {
    flex: 1,
    marginLeft: 12,
  },
  title: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#1a1a1a",
  },
  year: {
    fontSize: 14,
    color: "#666",
    marginTop: 2,
  },
  starRating: {
    fontSize: 14,
    color: "#FFA000",
    marginTop: 4,
  },
  starNumeric: {
    fontSize: 13,
    color: "#666",
  },
  voteCount: {
    fontSize: 12,
    color: "#888",
    marginTop: 2,
  },
  overview: {
    fontSize: 12,
    color: "#777",
    marginTop: 4,
    lineHeight: 17,
  },
  pill: {
    alignSelf: "flex-start",
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 2,
    marginTop: 6,
  },
  pillText: {
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },
});
