import React, { useEffect, useState } from "react";
import { ActivityIndicator, Alert, FlatList, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { useLibraryStore } from "@/store/libraryStore";
import { useStreamingKeysStore } from "@/store/streamingKeysStore";
import { searchSpotifyTracks, getSpotifyAudioFeatures, type SpotifySearchResult } from "@/services/spotify";
import { findBestYoutubeMatch } from "@/services/youtube";
import { buildYoutubeTrack } from "@/utils/importTrack";
import { isSpotifyConfigured, isYoutubeConfigured } from "@/config/streamingConfig";
import { colors } from "@/theme";

function KeysSettingsForm({ onSaved }: { onSaved?: () => void }) {
  const stored = useStreamingKeysStore((s) => ({
    spotifyClientId: s.spotifyClientId,
    spotifyClientSecret: s.spotifyClientSecret,
    youtubeApiKey: s.youtubeApiKey,
  }));
  const setKeys = useStreamingKeysStore((s) => s.setKeys);
  const [spotifyClientId, setSpotifyClientId] = useState(stored.spotifyClientId);
  const [spotifyClientSecret, setSpotifyClientSecret] = useState(stored.spotifyClientSecret);
  const [youtubeApiKey, setYoutubeApiKey] = useState(stored.youtubeApiKey);
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await setKeys({
        spotifyClientId: spotifyClientId.trim(),
        spotifyClientSecret: spotifyClientSecret.trim(),
        youtubeApiKey: youtubeApiKey.trim(),
      });
      onSaved?.();
    } finally {
      setSaving(false);
    }
  };

  return (
    <View style={styles.setupBox}>
      <Text style={styles.setupTitle}>Klucze Spotify + YouTube</Text>
      <Text style={styles.setupText}>
        Oba są darmowe, ale wymagają założenia własnego konta deweloperskiego — nikt nie może zrobić
        tego za Ciebie. Klucze zostają tylko na tym telefonie (w pamięci przeglądarki/appki), nigdzie nie
        są wysyłane poza Spotify/Google.
      </Text>

      <Text style={styles.setupStep}>1. developer.spotify.com/dashboard → Create app</Text>
      <Text style={styles.label}>Spotify Client ID</Text>
      <TextInput
        style={styles.input}
        value={spotifyClientId}
        onChangeText={setSpotifyClientId}
        autoCapitalize="none"
        autoCorrect={false}
        placeholder="np. a1b2c3d4e5f6…"
        placeholderTextColor={colors.textDim}
      />
      <Text style={styles.label}>Spotify Client Secret</Text>
      <TextInput
        style={styles.input}
        value={spotifyClientSecret}
        onChangeText={setSpotifyClientSecret}
        autoCapitalize="none"
        autoCorrect={false}
        secureTextEntry
        placeholder="Client Secret"
        placeholderTextColor={colors.textDim}
      />

      <Text style={styles.setupStep}>2. console.cloud.google.com → włącz „YouTube Data API v3” → Credentials</Text>
      <Text style={styles.label}>YouTube API Key</Text>
      <TextInput
        style={styles.input}
        value={youtubeApiKey}
        onChangeText={setYoutubeApiKey}
        autoCapitalize="none"
        autoCorrect={false}
        placeholder="AIza…"
        placeholderTextColor={colors.textDim}
      />

      <Pressable style={styles.saveButton} onPress={handleSave} disabled={saving}>
        {saving ? <ActivityIndicator color={colors.background} /> : <Text style={styles.saveButtonText}>Zapisz</Text>}
      </Pressable>
    </View>
  );
}

export function OnlineSearchScreen() {
  const addTrack = useLibraryStore((s) => s.addTrack);
  const tracks = useLibraryStore((s) => s.tracks);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<SpotifySearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [addingId, setAddingId] = useState<string | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [showSettings, setShowSettings] = useState(false);

  // Re-render this screen whenever keys change, so `configured` below picks it up immediately.
  useStreamingKeysStore((s) => s.spotifyClientId + s.spotifyClientSecret + s.youtubeApiKey);

  const configured = isSpotifyConfigured() && isYoutubeConfigured();
  const addedSpotifyIds = new Set(tracks.filter((t) => t.source === "youtube").map((t) => `${t.title}::${t.artist}`));

  useEffect(() => {
    if (!configured) setShowSettings(true);
  }, [configured]);

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

  if (showSettings) {
    return (
      <View style={styles.container}>
        <KeysSettingsForm onSaved={() => setShowSettings(false)} />
        {configured && (
          <Pressable style={styles.backLink} onPress={() => setShowSettings(false)}>
            <Text style={styles.backLinkText}>← Wróć do wyszukiwarki</Text>
          </Pressable>
        )}
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerRow}>
          <Text style={styles.headerTitle}>Szukaj online</Text>
          <Pressable onPress={() => setShowSettings(true)}>
            <Text style={styles.settingsLink}>Klucze</Text>
          </Pressable>
        </View>
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
  headerRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  headerTitle: { color: colors.text, fontSize: 18, fontWeight: "800" },
  settingsLink: { color: colors.accent, fontSize: 12, fontWeight: "700" },
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
  setupText: { color: colors.textDim, fontSize: 13, lineHeight: 19, marginBottom: 12 },
  setupStep: { color: colors.text, fontSize: 12, fontWeight: "700", marginTop: 10, marginBottom: 4 },
  label: { color: colors.textDim, fontSize: 12, marginBottom: 4, marginTop: 4 },
  input: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: colors.text,
    fontSize: 14,
  },
  saveButton: {
    backgroundColor: colors.accent,
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: "center",
    marginTop: 16,
  },
  saveButtonText: { color: colors.background, fontWeight: "800" },
  backLink: { padding: 16 },
  backLinkText: { color: colors.textDim, fontSize: 13, fontWeight: "700" },
});
