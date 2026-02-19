import React from "react";
import { View, Text, StyleSheet } from "react-native";

interface GenreChipProps {
  name: string;
}

export function GenreChip({ name }: GenreChipProps) {
  return (
    <View style={styles.chip}>
      <Text style={styles.label}>{name}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: "#f0f0f0",
    borderRadius: 16,
    marginRight: 8,
  },
  label: {
    fontSize: 13,
    color: "#333",
  },
});
