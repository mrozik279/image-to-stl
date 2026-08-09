import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "@/theme";
import { formatBpm } from "@/utils/format";
import type { Track } from "@/types";

interface Props {
  track: Track;
  onPress: () => void;
  onLoadA: () => void;
  onLoadB: () => void;
}

export function TrackListItem({ track, onPress, onLoadA, onLoadB }: Props) {
  return (
    <Pressable style={styles.row} onPress={onPress}>
      <View style={styles.info}>
        <Text style={styles.title} numberOfLines={1}>
          {track.title}
        </Text>
        <Text style={styles.meta} numberOfLines={1}>
          {track.artist} {track.genre ? `· ${track.genre}` : ""} · {formatBpm(track.bpm)} BPM
        </Text>
      </View>
      <View style={styles.actions}>
        <Pressable style={[styles.loadButton, { borderColor: colors.deckA }]} onPress={onLoadA}>
          <Text style={[styles.loadButtonText, { color: colors.deckA }]}>A</Text>
        </Pressable>
        <Pressable style={[styles.loadButton, { borderColor: colors.deckB }]} onPress={onLoadB}>
          <Text style={[styles.loadButtonText, { color: colors.deckB }]}>B</Text>
        </Pressable>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 12,
    marginBottom: 8,
  },
  info: { flex: 1, marginRight: 8 },
  title: { color: colors.text, fontSize: 15, fontWeight: "600" },
  meta: { color: colors.textDim, fontSize: 12, marginTop: 2 },
  actions: { flexDirection: "row", gap: 6 },
  loadButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    borderWidth: 1.5,
    alignItems: "center",
    justifyContent: "center",
  },
  loadButtonText: { fontWeight: "800" },
});
