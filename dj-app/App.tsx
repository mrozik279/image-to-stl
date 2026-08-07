import React, { useState } from "react";
import { Pressable, SafeAreaView, StatusBar, StyleSheet, Text, View } from "react-native";
import { LibraryScreen } from "@/screens/LibraryScreen";
import { MixerScreen } from "@/screens/MixerScreen";
import { SuggestionsScreen } from "@/screens/SuggestionsScreen";
import { colors } from "@/theme";

type Tab = "mixer" | "library" | "suggestions";

export default function App() {
  const [tab, setTab] = useState<Tab>("library");

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor={colors.background} />
      <View style={styles.appBar}>
        <Text style={styles.appTitle}>DJ Mixer</Text>
      </View>

      <View style={styles.content}>
        {tab === "mixer" ? <MixerScreen /> : tab === "suggestions" ? <SuggestionsScreen /> : <LibraryScreen />}
      </View>

      <View style={styles.tabBar}>
        <Pressable style={styles.tabButton} onPress={() => setTab("library")}>
          <Text style={[styles.tabText, tab === "library" && styles.tabTextActive]}>Biblioteka</Text>
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

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  appBar: { paddingHorizontal: 16, paddingTop: 8, paddingBottom: 4 },
  appTitle: { color: colors.text, fontSize: 20, fontWeight: "800" },
  content: { flex: 1 },
  tabBar: {
    flexDirection: "row",
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.surface,
  },
  tabButton: { flex: 1, paddingVertical: 14, alignItems: "center" },
  tabText: { color: colors.textDim, fontWeight: "700" },
  tabTextActive: { color: colors.accent },
});
