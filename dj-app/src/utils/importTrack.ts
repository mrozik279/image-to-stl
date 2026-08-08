import { Platform } from "react-native";
import * as DocumentPicker from "expo-document-picker";
import * as FileSystem from "expo-file-system";
import type { Track } from "@/types";
import type { CuratedTrack } from "@/data/curatedPlaylist";

const LIBRARY_DIR = `${FileSystem.documentDirectory}dj-app-tracks/`;

async function ensureLibraryDir() {
  const info = await FileSystem.getInfoAsync(LIBRARY_DIR);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(LIBRARY_DIR, { intermediates: true });
  }
}

/**
 * expo-file-system has no web implementation at all - calling it there
 * throws immediately. The web picker already hands back a `blob:` URL
 * that's directly playable, so there's nothing to copy; we just use it
 * as-is. It won't survive a page reload (blob URLs die with the tab), but
 * that's an inherent browser limitation, not something worth a fake
 * workaround for.
 */
function isWeb() {
  return Platform.OS === "web";
}

function titleFromFilename(name: string): { title: string; artist: string } {
  const base = name.replace(/\.[^/.]+$/, "");
  const parts = base.split(" - ");
  if (parts.length >= 2) {
    return { artist: parts[0].trim(), title: parts.slice(1).join(" - ").trim() };
  }
  return { artist: "Nieznany artysta", title: base };
}

/**
 * Opens the system file picker for audio files and copies each selection into
 * the app's sandboxed documents dir (required for stable playback across app
 * restarts, since picker URIs from some providers are transient). On web
 * there's no sandboxed filesystem to copy into, so the picker's own blob URL
 * is used directly.
 */
export async function pickAndImportTracks(): Promise<Track[]> {
  const result = await DocumentPicker.getDocumentAsync({
    type: "audio/*",
    multiple: true,
    copyToCacheDirectory: true,
  });

  if (result.canceled) return [];

  if (isWeb()) {
    return result.assets.map((asset) => {
      const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
      return buildTrack(id, asset.uri, asset.name);
    });
  }

  await ensureLibraryDir();

  const imported: Track[] = [];
  for (const asset of result.assets) {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
    const ext = asset.name.includes(".") ? asset.name.split(".").pop() : "audio";
    const destUri = `${LIBRARY_DIR}${id}.${ext}`;
    try {
      await FileSystem.copyAsync({ from: asset.uri, to: destUri });
    } catch (err) {
      // Fall back to the picker's own URI if copy fails (e.g. some cloud providers).
      imported.push(buildTrack(id, asset.uri, asset.name));
      continue;
    }
    imported.push(buildTrack(id, destUri, asset.name));
  }
  return imported;
}

/**
 * Links a locally-picked audio file to one curated suggestion, prefilling
 * title/artist/genre/bpm from the suggestion instead of guessing from the
 * filename. The user still supplies the actual audio - we only ship metadata.
 */
export async function importFileForCuratedTrack(curated: CuratedTrack): Promise<Track | null> {
  const result = await DocumentPicker.getDocumentAsync({
    type: "audio/*",
    multiple: false,
    copyToCacheDirectory: true,
  });
  if (result.canceled || result.assets.length === 0) return null;

  const asset = result.assets[0];
  const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
  let finalUri = asset.uri;

  if (!isWeb()) {
    await ensureLibraryDir();
    const ext = asset.name.includes(".") ? asset.name.split(".").pop() : "audio";
    const destUri = `${LIBRARY_DIR}${id}.${ext}`;
    try {
      await FileSystem.copyAsync({ from: asset.uri, to: destUri });
      finalUri = destUri;
    } catch (err) {
      // Fall back to the picker's own URI if copy fails (e.g. some cloud providers).
    }
  }

  return {
    id,
    title: curated.title,
    artist: curated.artist,
    source: "local",
    uri: finalUri,
    bpm: curated.approxBpm,
    genre: curated.genre,
    addedAt: Date.now(),
  };
}

function buildTrack(id: string, uri: string, filename: string): Track {
  const { title, artist } = titleFromFilename(filename);
  return {
    id,
    title,
    artist,
    source: "local",
    uri,
    addedAt: Date.now(),
  };
}

/** Adds a YouTube-backed track: no file to import, just the video ID + metadata. */
export function buildYoutubeTrack(params: {
  title: string;
  artist: string;
  youtubeVideoId: string;
  bpm?: number;
  genre?: string;
  key?: string;
}): Track {
  return {
    id: `yt-${params.youtubeVideoId}-${Date.now()}`,
    title: params.title,
    artist: params.artist,
    source: "youtube",
    youtubeVideoId: params.youtubeVideoId,
    bpm: params.bpm,
    genre: params.genre,
    key: params.key,
    addedAt: Date.now(),
  };
}
