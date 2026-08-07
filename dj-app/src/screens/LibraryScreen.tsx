import React, { useEffect, useState } from "react";
import { FlatList, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { useLibraryStore } from "@/store/libraryStore";
import { useMixerStore } from "@/store/mixerStore";
import { TrackListItem } from "@/components/TrackListItem";
import { TrackEditModal } from "@/components/TrackEditModal";
import { pickAndImportTracks } from "@/utils/importTrack";
import { colors } from "@/theme";
import type { Track, TrackSortKey } from "@/types";

const SORT_LABELS: { key: TrackSortKey; label: string }[] = [
  { key: "addedAt", label: "Dodane" },
  { key: "title", label: "Tytuł" },
  { key: "bpm", label: "BPM" },
  { key: "genre", label: "Gatunek" },
];

export function LibraryScreen() {
  const hydrate = useLibraryStore((s) => s.hydrate);
  const hydrated = useLibraryStore((s) => s.hydrated);
  const addTrack = useLibraryStore((s) => s.addTrack);
  const updateTrack = useLibraryStore((s) => s.updateTrack);
  const removeTrack = useLibraryStore((s) => s.removeTrack);
  const searchQuery = useLibraryStore((s) => s.searchQuery);
  const setSearchQuery = useLibraryStore((s) => s.setSearchQuery);
  const sortKey = useLibraryStore((s) => s.sortKey);
  const setSort = useLibraryStore((s) => s.setSort);
  const tracks = useLibraryStore((s) => s.visibleTracks());
  const loadTrack = useMixerStore((s) => s.loadTrack);

  const [editingTrack, setEditingTrack] = useState<Track | null>(null);
  const [importing, setImporting] = useState(false);

  useEffect(() => {
    if (!hydrated) hydrate();
  }, [hydrated, hydrate]);

  const handleImport = async () => {
    setImporting(true);
    try {
      const imported = await pickAndImportTracks();
      for (const track of imported) {
        await addTrack(track);
      }
    } finally {
      setImporting(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.topBar}>
        <TextInput
          style={styles.search}
          placeholder="Szukaj utworu, artysty, gatunku…"
          placeholderTextColor={colors.textDim}
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
        <Pressable style={styles.importButton} onPress={handleImport} disabled={importing}>
          <Text style={styles.importButtonText}>{importing ? "…" : "+ Import"}</Text>
        </Pressable>
      </View>

      <View style={styles.sortRow}>
        {SORT_LABELS.map((s) => (
          <Pressable key={s.key} style={styles.sortPill} onPress={() => setSort(s.key)}>
            <Text style={[styles.sortPillText, sortKey === s.key && { color: colors.accent }]}>{s.label}</Text>
          </Pressable>
        ))}
      </View>

      {tracks.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>
            Biblioteka jest pusta. Zaimportuj pliki audio przyciskiem „+ Import” powyżej.
          </Text>
        </View>
      ) : (
        <FlatList
          data={tracks}
          keyExtractor={(t) => t.id}
          contentContainerStyle={{ padding: 12 }}
          renderItem={({ item }) => (
            <TrackListItem
              track={item}
              onPress={() => setEditingTrack(item)}
              onLoadA={() => loadTrack("A", item.id)}
              onLoadB={() => loadTrack("B", item.id)}
            />
          )}
        />
      )}

      <TrackEditModal
        track={editingTrack}
        onClose={() => setEditingTrack(null)}
        onSave={(patch) => editingTrack && updateTrack(editingTrack.id, patch)}
        onDelete={() => {
          if (editingTrack) removeTrack(editingTrack.id);
          setEditingTrack(null);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  topBar: { flexDirection: "row", padding: 12, gap: 8 },
  search: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 10,
    color: colors.text,
  },
  importButton: {
    backgroundColor: colors.accent,
    borderRadius: 10,
    paddingHorizontal: 14,
    justifyContent: "center",
  },
  importButtonText: { color: colors.background, fontWeight: "800" },
  sortRow: { flexDirection: "row", paddingHorizontal: 12, gap: 14, marginBottom: 4 },
  sortPill: { paddingVertical: 4 },
  sortPillText: { color: colors.textDim, fontSize: 12, fontWeight: "700" },
  empty: { flex: 1, alignItems: "center", justifyContent: "center", padding: 32 },
  emptyText: { color: colors.textDim, textAlign: "center", lineHeight: 20 },
});
