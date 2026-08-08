import { STREAMING_SECRETS } from "@/config/streamingSecrets";

export function isSpotifyConfigured(): boolean {
  return Boolean(STREAMING_SECRETS.spotifyClientId && STREAMING_SECRETS.spotifyClientSecret);
}

export function isYoutubeConfigured(): boolean {
  return Boolean(STREAMING_SECRETS.youtubeApiKey);
}

export { STREAMING_SECRETS };
