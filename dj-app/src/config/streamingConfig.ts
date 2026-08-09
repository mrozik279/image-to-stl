import { STREAMING_SECRETS } from "@/config/streamingSecrets";
import { useStreamingKeysStore, type StreamingKeys } from "@/store/streamingKeysStore";

/**
 * Keys typed into the app (Online screen, saved via AsyncStorage) always win
 * over the compiled streamingSecrets.ts file - that file only helps someone
 * building from a computer who'd rather not retype keys after every reload.
 */
export function getStreamingKeys(): StreamingKeys {
  const runtime = useStreamingKeysStore.getState();
  return {
    spotifyClientId: runtime.spotifyClientId || STREAMING_SECRETS.spotifyClientId,
    spotifyClientSecret: runtime.spotifyClientSecret || STREAMING_SECRETS.spotifyClientSecret,
    youtubeApiKey: runtime.youtubeApiKey || STREAMING_SECRETS.youtubeApiKey,
  };
}

export function isSpotifyConfigured(): boolean {
  const keys = getStreamingKeys();
  return Boolean(keys.spotifyClientId && keys.spotifyClientSecret);
}

export function isYoutubeConfigured(): boolean {
  return Boolean(getStreamingKeys().youtubeApiKey);
}
