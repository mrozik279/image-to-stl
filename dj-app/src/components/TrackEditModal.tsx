import React, { useEffect, useState } from "react";
import { Modal, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { colors } from "@/theme";
import type { Track } from "@/types";

interface Props {
  track: Track | null;
  onClose: () => void;
  onSave: (patch: Partial<Track>) => void;
  onDelete: () => void;
}

export function TrackEditModal({ track, onClose, onSave, onDelete }: Props) {
  const [title, setTitle] = useState("");
  const [artist, setArtist] = useState("");
  const [bpm, setBpm] = useState("");
  const [genre, setGenre] = useState("");
  const [key, setKey] = useState("");

  useEffect(() => {
    if (track) {
      setTitle(track.title);
      setArtist(track.artist);
      setBpm(track.bpm ? String(track.bpm) : "");
      setGenre(track.genre ?? "");
      setKey(track.key ?? "");
    }
  }, [track]);

  if (!track) return null;

  const handleSave = () => {
    const parsedBpm = parseFloat(bpm.replace(",", "."));
    onSave({
      title: title.trim() || track.title,
      artist: artist.trim(),
      bpm: Number.isFinite(parsedBpm) && parsedBpm > 0 ? parsedBpm : undefined,
      genre: genre.trim() || undefined,
      key: key.trim() || undefined,
    });
    onClose();
  };

  return (
    <Modal visible transparent animationType="slide" onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <View style={styles.sheet}>
          <Text style={styles.header}>Edytuj utwór</Text>

          <Text style={styles.label}>Tytuł</Text>
          <TextInput style={styles.input} value={title} onChangeText={setTitle} placeholderTextColor={colors.textDim} />

          <Text style={styles.label}>Artysta</Text>
          <TextInput style={styles.input} value={artist} onChangeText={setArtist} placeholderTextColor={colors.textDim} />

          <View style={styles.rowFields}>
            <View style={{ flex: 1 }}>
              <Text style={styles.label}>BPM</Text>
              <TextInput
                style={styles.input}
                value={bpm}
                onChangeText={setBpm}
                keyboardType="decimal-pad"
                placeholder="np. 128"
                placeholderTextColor={colors.textDim}
              />
            </View>
            <View style={{ flex: 1, marginLeft: 8 }}>
              <Text style={styles.label}>Tonacja (Camelot)</Text>
              <TextInput
                style={styles.input}
                value={key}
                onChangeText={setKey}
                placeholder="np. 8A"
                placeholderTextColor={colors.textDim}
              />
            </View>
          </View>

          <Text style={styles.label}>Gatunek</Text>
          <TextInput style={styles.input} value={genre} onChangeText={setGenre} placeholderTextColor={colors.textDim} />

          <View style={styles.buttonRow}>
            <Pressable style={styles.deleteButton} onPress={onDelete}>
              <Text style={styles.deleteButtonText}>Usuń</Text>
            </Pressable>
            <View style={{ flex: 1 }} />
            <Pressable style={styles.cancelButton} onPress={onClose}>
              <Text style={styles.cancelButtonText}>Anuluj</Text>
            </Pressable>
            <Pressable style={styles.saveButton} onPress={handleSave}>
              <Text style={styles.saveButtonText}>Zapisz</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: "rgba(0,0,0,0.6)", justifyContent: "flex-end" },
  sheet: { backgroundColor: colors.surface, borderTopLeftRadius: 16, borderTopRightRadius: 16, padding: 20 },
  header: { color: colors.text, fontSize: 18, fontWeight: "700", marginBottom: 12 },
  label: { color: colors.textDim, fontSize: 12, marginBottom: 4, marginTop: 8 },
  input: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: colors.text,
    fontSize: 14,
  },
  rowFields: { flexDirection: "row" },
  buttonRow: { flexDirection: "row", alignItems: "center", marginTop: 20, gap: 8 },
  deleteButton: { paddingVertical: 10, paddingHorizontal: 14 },
  deleteButtonText: { color: colors.danger, fontWeight: "700" },
  cancelButton: { paddingVertical: 10, paddingHorizontal: 14 },
  cancelButtonText: { color: colors.textDim, fontWeight: "700" },
  saveButton: { backgroundColor: colors.accent, borderRadius: 8, paddingVertical: 10, paddingHorizontal: 18 },
  saveButtonText: { color: colors.background, fontWeight: "800" },
});
