import * as DocumentPicker from "expo-document-picker";
import * as FileSystem from "expo-file-system";
import type { Track } from "@/types";

const LIBRARY_DIR = `${FileSystem.documentDirectory}dj-app-tracks/`;

async function ensureLibraryDir() {
  const info = await FileSystem.getInfoAsync(LIBRARY_DIR);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(LIBRARY_DIR, { intermediates: true });
  }
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
 * restarts, since picker URIs from some providers are transient).
 */
export async function pickAndImportTracks(): Promise<Track[]> {
  const result = await DocumentPicker.getDocumentAsync({
    type: "audio/*",
    multiple: true,
    copyToCacheDirectory: true,
  });

  if (result.canceled) return [];
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

function buildTrack(id: string, uri: string, filename: string): Track {
  const { title, artist } = titleFromFilename(filename);
  return {
    id,
    title,
    artist,
    uri,
    addedAt: Date.now(),
  };
}
