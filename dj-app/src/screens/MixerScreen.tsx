import React from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { Deck } from "@/components/Deck";
import { Crossfader } from "@/components/Crossfader";
import { useMixerStore } from "@/store/mixerStore";
import { colors } from "@/theme";

// Two full decks side-by-side don't fit a phone-width screen (controls get
// squeezed unreadable), so decks stack vertically and scroll; the crossfader
// stays pinned below since it acts on both decks at once.
export function MixerScreen() {
  const crossfader = useMixerStore((s) => s.crossfader);
  const setCrossfader = useMixerStore((s) => s.setCrossfader);
  const masterVolume = useMixerStore((s) => s.masterVolume);

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.decksScroll}>
        <Deck deckId="A" />
        <Deck deckId="B" />
      </ScrollView>
      <Crossfader value={crossfader} onChange={setCrossfader} />
      <Text style={styles.masterLabel}>Master: {(masterVolume * 100).toFixed(0)}%</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  decksScroll: { padding: 6 },
  masterLabel: { color: colors.textDim, textAlign: "center", fontSize: 11, paddingBottom: 8 },
});
