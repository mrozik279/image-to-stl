import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { Deck } from "@/components/Deck";
import { Crossfader } from "@/components/Crossfader";
import { useMixerStore } from "@/store/mixerStore";
import { colors } from "@/theme";

export function MixerScreen() {
  const crossfader = useMixerStore((s) => s.crossfader);
  const setCrossfader = useMixerStore((s) => s.setCrossfader);
  const masterVolume = useMixerStore((s) => s.masterVolume);

  return (
    <View style={styles.container}>
      <View style={styles.decksRow}>
        <Deck deckId="A" />
        <Deck deckId="B" />
      </View>
      <Crossfader value={crossfader} onChange={setCrossfader} />
      <Text style={styles.masterLabel}>Master: {(masterVolume * 100).toFixed(0)}%</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  decksRow: { flex: 1, flexDirection: "row", padding: 6 },
  masterLabel: { color: colors.textDim, textAlign: "center", fontSize: 11, paddingBottom: 8 },
});
