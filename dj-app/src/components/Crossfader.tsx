import React from "react";
import { StyleSheet, Text, View } from "react-native";
import Slider from "@react-native-community/slider";
import { colors } from "@/theme";

interface Props {
  value: number;
  onChange: (value: number) => void;
}

export function Crossfader({ value, onChange }: Props) {
  return (
    <View style={styles.container}>
      <View style={styles.labels}>
        <Text style={[styles.label, { color: colors.deckA }]}>A</Text>
        <Text style={styles.title}>CROSSFADER</Text>
        <Text style={[styles.label, { color: colors.deckB }]}>B</Text>
      </View>
      <Slider
        style={styles.slider}
        minimumValue={-1}
        maximumValue={1}
        value={value}
        onValueChange={onChange}
        minimumTrackTintColor={colors.deckA}
        maximumTrackTintColor={colors.deckB}
        thumbTintColor={colors.text}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { paddingHorizontal: 16, paddingVertical: 8 },
  labels: { flexDirection: "row", justifyContent: "space-between", marginBottom: 4 },
  label: { fontWeight: "700", fontSize: 14 },
  title: { color: colors.textDim, fontSize: 11, letterSpacing: 1 },
  slider: { width: "100%", height: 32 },
});
