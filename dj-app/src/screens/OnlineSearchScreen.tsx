import React, { useState } from "react";
import { ActivityIndicator, Alert, FlatList, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { useLibraryStore } from "@/store/libraryStore";
import { searchSpotifyTracks, getSpotifyAudioFeatures, type SpotifySearchResult } from "@/services/spotify";
import { findBestYoutubeMatch } from "@/services/youtube";
import { buildYoutubeTrack } from "@/utils/importTrack";
import { isSpotifyConfigured, isYoutubeConfigured } from "@/config/streamingConfig";
import { colors } from "@/theme";

export function OnlineSearchScreen() {
  const addTrack = useLibraryStore((s) => s.addTrack);
  const tracks = useLibraryStore((s) => s.tracks);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<SpotifySearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [addingId, setAddingId] = useState<string | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);

  const configured = isSpotifyConfigured() && isYoutubeConfigured();
  const addedSpotifyIds = new Set(tracks.filter((t) => t.source === "youtube").map((t) => `${t.title}::${t.artist}`));

  const handleSearch = async () => {
    if (!query.trim()) return;
    setSearching(true);
    setSearchError(null);
    try {
      const found = await searchSpotifyTracks(query.trim());
      setResults(found);
    } catch (err) {
      setSearchError(String(err instanceof Error ? err.message : err));
    } finally {
      setSearching(false);
    }
  };

  const handleAdd = async (item: SpotifySearchResult) => {
    setAddingId(item.spotifyId);
    try {
      const match = await findBestYoutubeMatch(item.artist, item.title);
      if (!match) {
        Alert.alert("Nie znaleziono", "Brak wyniku na YouTube dla tego utworu.");
        return;
      }
      const features = await getSpotifyAudioFeatures(item.spotifyId);
      const track = buildYoutubeTrack({
        title: item.title,
        artist: item.artist,
        youtubeVideoId: match.videoId,
        bpm: features?.bpm,
        key: features?.key,
      });
      await addTrack(track);
    } catch (err) {
      Alert.alert("Import nie powiódł się", String(err instanceof Error ? err.message : err));
    } finally {
      setAddingId(null);
    }
  };

  if (!configured) {
    return (
      <View style={styles.container}>
        <View style={styles.setupBox}>
          <Text style={styles.setupTitle}>Wyszukiwanie online nieskonfigurowane</Text>
          <Text style={styles.setupText}>
            Uzupełnij <Text style={styles.mono}>src/config/streamingSecrets.ts</Text> (skopiowany z{" "}
            <Text style={styles.mono}>streamingSecrets.example.ts</Text>) własnym Spotify Client ID/Secret
            (developer.spotify.com) i kluczem YouTube Data API v3 (console.cloud.google.com). Oba są darmowe,
            ale wymagają Twojego konta.
          </Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Szukaj online</Text>
        <Text style={styles.headerSubtitle}>
          Wyszukiwarka Spotify (metadane) → dopasowanie na YouTube (odtwarzanie). Nic nie jest pobierane na dysk.
        </Text>
      </View>

      <View style={styles.searchRow}>
        <TextInput
          style={styles.searchInput}
          placeholder="Tytuł, artysta…"
          placeholderTextColor={colors.textDim}
          value={query}
          onChangeText={setQuery}
          onSubmitEditing={handleSearch}
          returnKeyType="search"
        />
        <Pressable style={styles.searchButton} onPress={handleSearch} disabled={searching}>
          {searching ? <ActivityIndicator color={colors.background} /> : <Text style={styles.searchButtonText}>Szukaj</Text>}
        </Pressable>
      </View>

      {searchError && <Text style={styles.errorText}>{searchError}</Text>}

      <FlatList
        data={results}
        keyExtractor={(item) => item.spotifyId}
        contentContainerStyle={{ padding: 12 }}
        renderItem={({ item }) => {
          const isAdded = addedSpotifyIds.has(`${item.title}::${item.artist}`);
          const isAdding = addingId === item.spotifyId;
          return (
            <View style={styles.row}>
              <View style={styles.info}>
                <Text style={styles.title} numberOfLines={1}>
                  {item.title}
                </Text>
                <Text style={styles.meta} numberOfLines={1}>
                  {item.artist}
                  {item.year ? ` · ${item.year}` : ""} · {item.album}
                </Text>
              </View>
              <Pressable style={[styles.addButton, isAdded && styles.addButtonDone]} disabled={isAdding} onPress={() => handleAdd(item)}>
                {isAdding ? (
                  <ActivityIndicator size="small" color={colors.text} />
                ) : (
                  <Text style={[styles.addButtonText, isAdded && styles.addButtonTextDone]}>
                    {isAdded ? "Dodano" : "+ YouTube"}
                  </Text>
                )}
              </Pressable>
            </View>
          );
        }}
        ListEmptyComponent={
          !searching ? <Text style={styles.emptyText}>Wpisz tytuł lub artystę i naciśnij „Szukaj”.</Text> : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: { padding: 16, paddingBottom: 8 },
  headerTitle: { color: colors.text, fontSize: 18, fontWeight: "800" },
  headerSubtitle: { color: colors.textDim, fontSize: 12, marginTop: 6, lineHeight: 17 },
  searchRow: { flexDirection: "row", paddingHorizontal: 16, gap: 8, marginTop: 4 },
  searchInput: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 10,
    color: colors.text,
  },
  searchButton: {
    backgroundColor: colors.accent,
    borderRadius: 10,
    paddingHorizontal: 16,
    justifyContent: "center",
    minWidth: 78,
    alignItems: "center",
  },
  searchButtonText: { color: colors.background, fontWeight: "800" },
  errorText: { color: colors.danger, fontSize: 12, paddingHorizontal: 16, marginTop: 8 },
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
  addButton: {
    borderRadius: 8,
    paddingVertical: 8,
    paddingHorizontal: 10,
    backgroundColor: colors.surfaceAlt,
    borderWidth: 1,
    borderColor: colors.border,
    minWidth: 84,
    alignItems: "center",
  },
  addButtonDone: { borderColor: colors.success, backgroundColor: "transparent" },
  addButtonText: { color: colors.text, fontSize: 11, fontWeight: "700" },
  addButtonTextDone: { color: colors.success },
  emptyText: { color: colors.textDim, textAlign: "center", marginTop: 24 },
  setupBox: { margin: 16, padding: 16, backgroundColor: colors.surface, borderRadius: 12 },
  setupTitle: { color: colors.accent, fontWeight: "800", fontSize: 15, marginBottom: 8 },
  setupText: { color: colors.textDim, fontSize: 13, lineHeight: 19 },
  mono: { color: colors.text, fontFamily: "monospace" },
});
