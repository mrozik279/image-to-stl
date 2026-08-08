import React, { useState } from "react";
import { Pressable, SafeAreaView, StatusBar, StyleSheet, Text, View } from "react-native";
import { LibraryScreen } from "@/screens/LibraryScreen";
import { MixerScreen } from "@/screens/MixerScreen";
import { SuggestionsScreen } from "@/screens/SuggestionsScreen";
import { OnlineSearchScreen } from "@/screens/OnlineSearchScreen";
import { YoutubeDeckPlayerView } from "@/components/YoutubeDeckPlayerView";
import { youtubeEngines } from "@/audio/YoutubeDeckEngine";
import { useMixerStore } from "@/store/mixerStore";
import { colors } from "@/theme";
import type { DeckId } from "@/types";

type Tab = "mixer" | "library" | "suggestions" | "online";

export default function App() {
  const [tab, setTab] = useState<Tab>("library");
  const sourceA = useMixerStore((s) => s.decks.A.source);
  const sourceB = useMixerStore((s) => s.decks.B.source);
  const anyYoutubeLoaded = sourceA === "youtube" || sourceB === "youtube";

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor={colors.background} />
      <View style={styles.appBar}>
        <Text style={styles.appTitle}>DJ Mixer</Text>
      </View>

      {/*
        Mounted for the app's whole lifetime (not just while the Mikser tab
        is visible) so switching tabs to search for the next track doesn't
        kill whatever is currently playing through YouTube.
      */}
      <View style={[styles.streamStrip, !anyYoutubeLoaded && styles.streamStripCollapsed]}>
        <StreamDeckSlot deckId="A" accent={colors.deckA} />
        <StreamDeckSlot deckId="B" accent={colors.deckB} />
      </View>

      <View style={styles.content}>
        {tab === "mixer" ? (
          <MixerScreen />
        ) : tab === "suggestions" ? (
          <SuggestionsScreen />
        ) : tab === "online" ? (
          <OnlineSearchScreen />
        ) : (
          <LibraryScreen />
        )}
      </View>

      <View style={styles.tabBar}>
        <Pressable style={styles.tabButton} onPress={() => setTab("library")}>
          <Text style={[styles.tabText, tab === "library" && styles.tabTextActive]}>Biblioteka</Text>
        </Pressable>
        <Pressable style={styles.tabButton} onPress={() => setTab("online")}>
          <Text style={[styles.tabText, tab === "online" && styles.tabTextActive]}>Online</Text>
        </Pressable>
        <Pressable style={styles.tabButton} onPress={() => setTab("suggestions")}>
          <Text style={[styles.tabText, tab === "suggestions" && styles.tabTextActive]}>Sugestie</Text>
        </Pressable>
        <Pressable style={styles.tabButton} onPress={() => setTab("mixer")}>
          <Text style={[styles.tabText, tab === "mixer" && styles.tabTextActive]}>Mikser</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

function StreamDeckSlot({ deckId, accent }: { deckId: DeckId; accent: string }) {
  const source = useMixerStore((s) => s.decks[deckId].source);
  if (source !== "youtube") return null;
  return (
    <View style={[styles.streamSlot, { borderColor: accent }]}>
      <Text style={[styles.streamSlotLabel, { color: accent }]}>{deckId}</Text>
      <YoutubeDeckPlayerView engine={youtubeEngines[deckId]} accent={accent} />
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  appBar: { paddingHorizontal: 16, paddingTop: 8, paddingBottom: 4 },
  appTitle: { color: colors.text, fontSize: 20, fontWeight: "800" },
  streamStrip: {
    flexDirection: "row",
    gap: 6,
    paddingHorizontal: 6,
    height: 84,
  },
  streamStripCollapsed: { height: 0, overflow: "hidden" },
  streamSlot: {
    flex: 1,
    borderWidth: 1,
    borderRadius: 6,
    overflow: "hidden",
    position: "relative",
  },
  streamSlotLabel: {
    position: "absolute",
    top: 2,
    left: 4,
    fontSize: 9,
    fontWeight: "800",
    zIndex: 1,
  },
  content: { flex: 1 },
  tabBar: {
    flexDirection: "row",
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.surface,
  },
  tabButton: { flex: 1, paddingVertical: 14, alignItems: "center" },
  tabText: { color: colors.textDim, fontWeight: "700", fontSize: 12 },
  tabTextActive: { color: colors.accent },
});
