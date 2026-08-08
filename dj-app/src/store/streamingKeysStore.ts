import AsyncStorage from "@react-native-async-storage/async-storage";
import { create } from "zustand";

const STORAGE_KEY = "dj-app.streamingKeys.v1";

export interface StreamingKeys {
  spotifyClientId: string;
  spotifyClientSecret: string;
  youtubeApiKey: string;
}

interface StreamingKeysState extends StreamingKeys {
  hydrated: boolean;
  hydrate: () => Promise<void>;
  setKeys: (keys: Partial<StreamingKeys>) => Promise<void>;
  clearKeys: () => Promise<void>;
}

/**
 * Keys entered directly in the app (Online screen) and persisted via
 * AsyncStorage - the whole point being someone with only a phone, no
 * computer, can configure Spotify/YouTube without ever touching
 * streamingSecrets.ts. Read by services/spotify.ts and services/youtube.ts
 * through getStreamingKeys() in config/streamingConfig.ts.
 */
export const useStreamingKeysStore = create<StreamingKeysState>((set, get) => ({
  spotifyClientId: "",
  spotifyClientSecret: "",
  youtubeApiKey: "",
  hydrated: false,

  hydrate: async () => {
    if (get().hydrated) return;
    const raw = await AsyncStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        const parsed = JSON.parse(raw);
        set({
          spotifyClientId: parsed.spotifyClientId ?? "",
          spotifyClientSecret: parsed.spotifyClientSecret ?? "",
          youtubeApiKey: parsed.youtubeApiKey ?? "",
          hydrated: true,
        });
        return;
      } catch {
        // fall through to marking hydrated with defaults
      }
    }
    set({ hydrated: true });
  },

  setKeys: async (keys) => {
    const next: StreamingKeys = {
      spotifyClientId: keys.spotifyClientId ?? get().spotifyClientId,
      spotifyClientSecret: keys.spotifyClientSecret ?? get().spotifyClientSecret,
      youtubeApiKey: keys.youtubeApiKey ?? get().youtubeApiKey,
    };
    set(next);
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  },

  clearKeys: async () => {
    set({ spotifyClientId: "", spotifyClientSecret: "", youtubeApiKey: "" });
    await AsyncStorage.removeItem(STORAGE_KEY);
  },
}));
