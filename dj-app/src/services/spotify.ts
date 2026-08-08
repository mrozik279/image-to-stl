import { STREAMING_SECRETS } from "@/config/streamingConfig";

export interface SpotifySearchResult {
  spotifyId: string;
  title: string;
  artist: string;
  album: string;
  year?: number;
  durationMs: number;
  popularity: number;
  albumArtUrl?: string;
}

let cachedToken: { value: string; expiresAt: number } | null = null;

/**
 * Client Credentials flow - app-level auth, no Spotify user login needed.
 * Only unlocks catalog search/browsing, not playback (which is the point:
 * we use Spotify purely for metadata/discovery, YouTube for playback).
 */
async function getAccessToken(): Promise<string> {
  if (cachedToken && cachedToken.expiresAt > Date.now() + 5000) {
    return cachedToken.value;
  }

  const basic = base64Encode(`${STREAMING_SECRETS.spotifyClientId}:${STREAMING_SECRETS.spotifyClientSecret}`);
  const res = await fetch("https://accounts.spotify.com/api/token", {
    method: "POST",
    headers: {
      Authorization: `Basic ${basic}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: "grant_type=client_credentials",
  });

  if (!res.ok) {
    throw new Error(`Spotify token request failed: ${res.status} ${await res.text()}`);
  }

  const json = await res.json();
  cachedToken = { value: json.access_token, expiresAt: Date.now() + json.expires_in * 1000 };
  return cachedToken.value;
}

const BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

/** Hermes (React Native's JS engine) doesn't ship btoa, so encode manually. */
function base64Encode(input: string): string {
  const bytes = utf8Bytes(input);
  let output = "";
  for (let i = 0; i < bytes.length; i += 3) {
    const b0 = bytes[i];
    const b1 = i + 1 < bytes.length ? bytes[i + 1] : undefined;
    const b2 = i + 2 < bytes.length ? bytes[i + 2] : undefined;

    output += BASE64_CHARS[b0 >> 2];
    output += BASE64_CHARS[((b0 & 0x03) << 4) | ((b1 ?? 0) >> 4)];
    output += b1 !== undefined ? BASE64_CHARS[((b1 & 0x0f) << 2) | ((b2 ?? 0) >> 6)] : "=";
    output += b2 !== undefined ? BASE64_CHARS[b2 & 0x3f] : "=";
  }
  return output;
}

function utf8Bytes(input: string): number[] {
  const bytes: number[] = [];
  for (let i = 0; i < input.length; i++) {
    const code = input.charCodeAt(i);
    if (code < 0x80) {
      bytes.push(code);
    } else if (code < 0x800) {
      bytes.push(0xc0 | (code >> 6), 0x80 | (code & 0x3f));
    } else {
      bytes.push(0xe0 | (code >> 12), 0x80 | ((code >> 6) & 0x3f), 0x80 | (code & 0x3f));
    }
  }
  return bytes;
}

export async function searchSpotifyTracks(query: string, limit = 15): Promise<SpotifySearchResult[]> {
  const token = await getAccessToken();
  const url = `https://api.spotify.com/v1/search?q=${encodeURIComponent(query)}&type=track&limit=${limit}`;
  const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });

  if (!res.ok) {
    throw new Error(`Spotify search failed: ${res.status} ${await res.text()}`);
  }

  const json = await res.json();
  const items: any[] = json.tracks?.items ?? [];
  return items.map((item) => ({
    spotifyId: item.id,
    title: item.name,
    artist: (item.artists ?? []).map((a: any) => a.name).join(", "),
    album: item.album?.name ?? "",
    year: item.album?.release_date ? parseInt(item.album.release_date.slice(0, 4), 10) : undefined,
    durationMs: item.duration_ms,
    popularity: item.popularity ?? 0,
    albumArtUrl: item.album?.images?.[0]?.url,
  }));
}

/**
 * Best-effort BPM/key lookup. Spotify restricted the audio-features endpoint
 * to apps with "extended quota mode" approval in Nov 2024 - a fresh app in
 * Development Mode will likely get a 403 here. We swallow that and just
 * return null so the caller falls back to tap-tempo instead of crashing.
 */
export async function getSpotifyAudioFeatures(spotifyId: string): Promise<{ bpm: number; key?: string } | null> {
  try {
    const token = await getAccessToken();
    const res = await fetch(`https://api.spotify.com/v1/audio-features/${spotifyId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return null;
    const json = await res.json();
    if (typeof json.tempo !== "number") return null;
    return { bpm: Math.round(json.tempo * 10) / 10, key: camelotFromSpotifyKey(json.key, json.mode) };
  } catch {
    return null;
  }
}

const CAMELOT_MAJOR = ["8B", "3B", "10B", "5B", "12B", "7B", "2B", "9B", "4B", "11B", "6B", "1B"];
const CAMELOT_MINOR = ["5A", "12A", "7A", "2A", "9A", "4A", "11A", "6A", "1A", "8A", "3A", "10A"];

function camelotFromSpotifyKey(key: number, mode: number): string | undefined {
  if (key < 0 || key > 11) return undefined;
  return mode === 1 ? CAMELOT_MAJOR[key] : CAMELOT_MINOR[key];
}
