/**
 * Copy this file to `streamingSecrets.ts` (already git-ignored) and fill in
 * your own keys. Both are free to obtain but require you to create your own
 * developer app — nobody else's key can be embedded here for you:
 *
 * - Spotify: https://developer.spotify.com/dashboard -> Create app ->
 *   Client ID + Client Secret (Client Credentials flow; no user login
 *   needed, search/catalog access only). New/dev-mode Spotify apps no
 *   longer get audio-features (BPM/key) access as of Nov 2024 - search
 *   still works fine, audio-features is best-effort and may 403.
 *
 * - YouTube: https://console.cloud.google.com -> new project -> enable
 *   "YouTube Data API v3" -> Credentials -> API key. Free quota (10,000
 *   units/day by default), a search call costs 100 units.
 *
 * SECURITY NOTE: this file ships inside the app bundle, so the Spotify
 * client secret is technically extractable by anyone who decompiles the
 * app. That's an acceptable tradeoff for a personal/local DJ tool but NOT
 * for a public app-store release - a real release should proxy the Spotify
 * token exchange through a small backend instead of embedding the secret
 * client-side.
 */
export const STREAMING_SECRETS = {
  spotifyClientId: "",
  spotifyClientSecret: "",
  youtubeApiKey: "",
};
