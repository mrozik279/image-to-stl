import React, { useState } from "react";
import { Alert, Pressable, SectionList, StyleSheet, Text, View } from "react-native";
import { useLibraryStore } from "@/store/libraryStore";
import { importFileForCuratedTrack } from "@/utils/importTrack";
import { CURATED_PLAYLIST, DECADES, type CuratedTrack } from "@/data/curatedPlaylist";
import { colors } from "@/theme";

const sections = DECADES.map((decade) => ({
  title: decade,
  data: CURATED_PLAYLIST.filter((t) => t.decade === decade),
}));

export function SuggestionsScreen() {
  const addTrack = useLibraryStore((s) => s.addTrack);
  const tracks = useLibraryStore((s) => s.tracks);
  const [linkingId, setLinkingId] = useState<string | null>(null);

  const linkedTitles = new Set(tracks.map((t) => `${t.title}::${t.artist}`));

  const handleLink = async (curated: CuratedTrack) => {
    setLinkingId(curated.id);
    try {
      const track = await importFileForCuratedTrack(curated);
      if (track) await addTrack(track);
    } catch (err) {
      Alert.alert("Import nie powiódł się", String(err));
    } finally {
      setLinkingId(null);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Najlepsze utwory 1960–2020</Text>
        <Text style={styles.headerSubtitle}>
          Same metadane (tytuł, artysta, orientacyjne BPM) — bez audio. Podłącz własny, legalnie
          posiadany plik przyciskiem „Podłącz plik”; BPM potwierdź potem przyciskiem TAP BPM na
          decku.
        </Text>
      </View>

      <SectionList
        sections={sections}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ padding: 12 }}
        renderSectionHeader={({ section }) => <Text style={styles.decadeHeader}>{section.title}</Text>}
        renderItem={({ item }) => {
          const isLinked = linkedTitles.has(`${item.title}::${item.artist}`);
          const isLinking = linkingId === item.id;
          return (
            <View style={styles.row}>
              <View style={styles.info}>
                <Text style={styles.title} numberOfLines={1}>
                  {item.title}
                </Text>
                <Text style={styles.meta} numberOfLines={1}>
                  {item.artist} · {item.year} · {item.genre} · ~{item.approxBpm} BPM
                </Text>
              </View>
              <Pressable
                style={[styles.linkButton, isLinked && styles.linkButtonDone]}
                disabled={isLinking}
                onPress={() => handleLink(item)}
              >
                <Text style={[styles.linkButtonText, isLinked && styles.linkButtonTextDone]}>
                  {isLinking ? "…" : isLinked ? "W bibliotece" : "Podłącz plik"}
                </Text>
              </Pressable>
            </View>
          );
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: { padding: 16, paddingBottom: 8 },
  headerTitle: { color: colors.text, fontSize: 18, fontWeight: "800" },
  headerSubtitle: { color: colors.textDim, fontSize: 12, marginTop: 6, lineHeight: 17 },
  decadeHeader: {
    color: colors.accent,
    fontWeight: "800",
    fontSize: 13,
    letterSpacing: 1,
    marginTop: 14,
    marginBottom: 6,
  },
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
  linkButton: {
    borderRadius: 8,
    paddingVertical: 8,
    paddingHorizontal: 10,
    backgroundColor: colors.surfaceAlt,
    borderWidth: 1,
    borderColor: colors.border,
  },
  linkButtonDone: { borderColor: colors.success, backgroundColor: "transparent" },
  linkButtonText: { color: colors.text, fontSize: 11, fontWeight: "700" },
  linkButtonTextDone: { color: colors.success },
});
