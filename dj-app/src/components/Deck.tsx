import React, { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import Slider from "@react-native-community/slider";
import { useMixerStore, MIN_RATE, MAX_RATE } from "@/store/mixerStore";
import { useLibraryStore } from "@/store/libraryStore";
import { colors } from "@/theme";
import { formatBpm, formatTime } from "@/utils/format";
import type { DeckId } from "@/types";

const LOOP_OPTIONS = [1, 2, 4, 8];

interface Props {
  deckId: DeckId;
}

export function Deck({ deckId }: Props) {
  const deck = useMixerStore((s) => s.decks[deckId]);
  const accent = deckId === "A" ? colors.deckA : colors.deckB;
  const track = useLibraryStore((s) => s.tracks.find((t) => t.id === deck.trackId));
  const [seekPreview, setSeekPreview] = useState<number | null>(null);

  const togglePlay = useMixerStore((s) => s.togglePlay);
  const seek = useMixerStore((s) => s.seek);
  const setDeckVolume = useMixerStore((s) => s.setDeckVolume);
  const setRate = useMixerStore((s) => s.setRate);
  const toggleKeylock = useMixerStore((s) => s.toggleKeylock);
  const tapTempo = useMixerStore((s) => s.tapTempo);
  const syncToOtherDeck = useMixerStore((s) => s.syncToOtherDeck);
  const setLoopBeats = useMixerStore((s) => s.setLoopBeats);
  const toggleLoop = useMixerStore((s) => s.toggleLoop);
  const setCue = useMixerStore((s) => s.setCue);
  const jumpToCue = useMixerStore((s) => s.jumpToCue);
  const triggerBrake = useMixerStore((s) => s.triggerBrake);
  const triggerEchoOut = useMixerStore((s) => s.triggerEchoOut);
  const [keylockOn, setKeylockOn] = useState(false);
  const [activeLoopBeats, setActiveLoopBeats] = useState<number | null>(null);

  const effectBusy = deck.effectActive !== null;
  const position = seekPreview ?? deck.positionMillis;

  return (
    <View style={[styles.container, { borderColor: accent }]}>
      <View style={styles.header}>
        <Text style={[styles.deckLabel, { color: accent }]}>DECK {deckId}</Text>
        <Text style={styles.bpm}>{formatBpm(track?.bpm)} BPM</Text>
      </View>

      <Text style={styles.title} numberOfLines={1}>
        {track ? track.title : "Brak utworu — wybierz z biblioteki"}
      </Text>
      <Text style={styles.artist} numberOfLines={1}>
        {track?.artist ?? ""}
      </Text>

      <View style={styles.progressRow}>
        <Text style={styles.time}>{formatTime(position)}</Text>
        <Slider
          style={styles.progressSlider}
          minimumValue={0}
          maximumValue={Math.max(deck.durationMillis, 1)}
          value={position}
          onValueChange={setSeekPreview}
          onSlidingComplete={(v) => {
            seek(deckId, v);
            setSeekPreview(null);
          }}
          minimumTrackTintColor={accent}
          maximumTrackTintColor={colors.border}
          thumbTintColor={accent}
          disabled={!deck.isLoaded}
        />
        <Text style={styles.time}>{formatTime(deck.durationMillis)}</Text>
      </View>

      <View style={styles.transportRow}>
        <Pressable
          style={[styles.playButton, { backgroundColor: accent }]}
          disabled={!deck.isLoaded || effectBusy}
          onPress={() => togglePlay(deckId)}
        >
          <Text style={styles.playButtonText}>{deck.isPlaying ? "PAUZA" : "PLAY"}</Text>
        </Pressable>
        <Pressable style={styles.smallButton} onPress={() => tapTempo(deckId)}>
          <Text style={styles.smallButtonText}>TAP BPM</Text>
        </Pressable>
        <Pressable style={styles.smallButton} onPress={() => syncToOtherDeck(deckId)}>
          <Text style={styles.smallButtonText}>SYNC</Text>
        </Pressable>
      </View>

      <View style={styles.sectionLabelRow}>
        <Text style={styles.sectionLabel}>PITCH {(deck.rate * 100 - 100).toFixed(1)}%</Text>
        <Pressable
          onPress={() => {
            setKeylockOn(!keylockOn);
            toggleKeylock(deckId, !keylockOn);
          }}
        >
          <Text style={[styles.toggleText, keylockOn && { color: accent }]}>KEYLOCK</Text>
        </Pressable>
      </View>
      <Slider
        style={styles.pitchSlider}
        minimumValue={MIN_RATE}
        maximumValue={MAX_RATE}
        value={deck.rate}
        onValueChange={(v) => setRate(deckId, v)}
        minimumTrackTintColor={accent}
        maximumTrackTintColor={colors.border}
        thumbTintColor={accent}
      />

      <Text style={styles.sectionLabel}>LOOP (beaty)</Text>
      <View style={styles.rowWrap}>
        {LOOP_OPTIONS.map((beats) => (
          <Pressable
            key={beats}
            style={[styles.pill, activeLoopBeats === beats && { backgroundColor: accent }]}
            onPress={() => {
              const next = activeLoopBeats === beats ? null : beats;
              setActiveLoopBeats(next);
              setLoopBeats(deckId, next);
            }}
          >
            <Text style={[styles.pillText, activeLoopBeats === beats && { color: colors.background }]}>
              {beats}
            </Text>
          </Pressable>
        ))}
        <Pressable style={styles.pill} onPress={() => toggleLoop(deckId)}>
          <Text style={styles.pillText}>{deck.loop.active ? "LOOP ON" : "LOOP OFF"}</Text>
        </Pressable>
      </View>

      <Text style={styles.sectionLabel}>HOT CUE</Text>
      <View style={styles.rowWrap}>
        {deck.cues.map((cue, i) => (
          <Pressable
            key={i}
            style={[styles.pill, cue && { borderColor: accent }]}
            onPress={() => (cue ? jumpToCue(deckId, i) : setCue(deckId, i))}
            onLongPress={() => setCue(deckId, i)}
          >
            <Text style={styles.pillText}>{i + 1}</Text>
          </Pressable>
        ))}
      </View>

      <Text style={styles.sectionLabel}>EFEKTY</Text>
      <View style={styles.rowWrap}>
        <Pressable
          style={[styles.pill, deck.effectActive === "brake" && { backgroundColor: accent }]}
          disabled={!deck.isPlaying || effectBusy}
          onPress={() => triggerBrake(deckId)}
        >
          <Text style={styles.pillText}>BRAKE</Text>
        </Pressable>
        <Pressable
          style={[styles.pill, deck.effectActive === "echo" && { backgroundColor: accent }]}
          disabled={!deck.isPlaying || effectBusy}
          onPress={() => triggerEchoOut(deckId)}
        >
          <Text style={styles.pillText}>ECHO OUT</Text>
        </Pressable>
      </View>

      <Text style={styles.sectionLabel}>VOLUME</Text>
      <Slider
        style={styles.pitchSlider}
        minimumValue={0}
        maximumValue={1}
        value={deck.volume}
        onValueChange={(v) => setDeckVolume(deckId, v)}
        minimumTrackTintColor={accent}
        maximumTrackTintColor={colors.border}
        thumbTintColor={accent}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
    margin: 6,
  },
  header: { flexDirection: "row", justifyContent: "space-between", marginBottom: 6 },
  deckLabel: { fontWeight: "800", fontSize: 13, letterSpacing: 1 },
  bpm: { color: colors.text, fontWeight: "700" },
  title: { color: colors.text, fontSize: 15, fontWeight: "600" },
  artist: { color: colors.textDim, fontSize: 12, marginBottom: 8 },
  progressRow: { flexDirection: "row", alignItems: "center" },
  progressSlider: { flex: 1, height: 28, marginHorizontal: 4 },
  time: { color: colors.textDim, fontSize: 11, width: 36, textAlign: "center" },
  transportRow: { flexDirection: "row", gap: 8, marginTop: 8, marginBottom: 10 },
  playButton: { flex: 1, borderRadius: 8, paddingVertical: 10, alignItems: "center" },
  playButtonText: { color: colors.background, fontWeight: "800" },
  smallButton: {
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 10,
    backgroundColor: colors.surfaceAlt,
    justifyContent: "center",
  },
  smallButtonText: { color: colors.text, fontWeight: "700", fontSize: 11 },
  sectionLabelRow: { flexDirection: "row", justifyContent: "space-between", marginTop: 6 },
  sectionLabel: { color: colors.textDim, fontSize: 11, letterSpacing: 1, marginTop: 8, marginBottom: 4 },
  toggleText: { color: colors.textDim, fontSize: 11, fontWeight: "700" },
  pitchSlider: { width: "100%", height: 26 },
  rowWrap: { flexDirection: "row", flexWrap: "wrap", gap: 6 },
  pill: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 6,
    paddingVertical: 6,
    paddingHorizontal: 10,
    backgroundColor: colors.surfaceAlt,
  },
  pillText: { color: colors.text, fontSize: 12, fontWeight: "700" },
});
