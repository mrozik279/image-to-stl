import React, { useEffect, useRef } from "react";
import { Platform, StyleSheet, Text, View } from "react-native";
import WebView from "react-native-webview";
import { YOUTUBE_BRIDGE_HTML } from "@/audio/youtubeBridgeHtml";
import type { YoutubeDeckEngine } from "@/audio/YoutubeDeckEngine";
import { colors } from "@/theme";

interface Props {
  engine: YoutubeDeckEngine;
  accent: string;
}

/**
 * react-native-webview has no web target (native iOS/Android/macOS/Windows
 * only), so the browser preview build of this app can't host a real YouTube
 * player - it shows a explanatory placeholder instead of crashing.
 */
export function YoutubeDeckPlayerView({ engine, accent }: Props) {
  const ref = useRef<WebView>(null);

  useEffect(() => {
    engine.attachWebView(ref.current);
    return () => engine.attachWebView(null);
  }, [engine]);

  if (Platform.OS === "web") {
    return (
      <View style={[styles.fallback, { borderColor: accent }]}>
        <Text style={styles.fallbackText}>Odtwarzacz YouTube wymaga natywnej appki (Expo Go / build), nie działa w podglądzie web.</Text>
      </View>
    );
  }

  return (
    <WebView
      ref={ref}
      originWhitelist={["*"]}
      source={{ html: YOUTUBE_BRIDGE_HTML, baseUrl: "https://www.youtube.com" }}
      onMessage={(e) => engine.handleBridgeMessage(e.nativeEvent.data)}
      allowsInlineMediaPlayback
      mediaPlaybackRequiresUserAction={false}
      style={styles.webview}
    />
  );
}

const styles = StyleSheet.create({
  webview: { flex: 1, backgroundColor: "#000" },
  fallback: {
    flex: 1,
    borderWidth: 1,
    borderRadius: 8,
    alignItems: "center",
    justifyContent: "center",
    padding: 8,
    backgroundColor: "#000",
  },
  fallbackText: { color: colors.textDim, fontSize: 10, textAlign: "center" },
});
