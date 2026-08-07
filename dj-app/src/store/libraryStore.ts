import AsyncStorage from "@react-native-async-storage/async-storage";
import { create } from "zustand";
import type { Track, TrackSortKey } from "@/types";

const STORAGE_KEY = "dj-app.library.v1";

interface LibraryState {
  tracks: Track[];
  searchQuery: string;
  sortKey: TrackSortKey;
  sortAsc: boolean;
  hydrated: boolean;
  hydrate: () => Promise<void>;
  addTrack: (track: Track) => Promise<void>;
  updateTrack: (id: string, patch: Partial<Track>) => Promise<void>;
  removeTrack: (id: string) => Promise<void>;
  setSearchQuery: (q: string) => void;
  setSort: (key: TrackSortKey) => void;
  visibleTracks: () => Track[];
}

async function persist(tracks: Track[]) {
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(tracks));
}

export const useLibraryStore = create<LibraryState>((set, get) => ({
  tracks: [],
  searchQuery: "",
  sortKey: "addedAt",
  sortAsc: false,
  hydrated: false,

  hydrate: async () => {
    const raw = await AsyncStorage.getItem(STORAGE_KEY);
    const tracks: Track[] = raw ? JSON.parse(raw) : [];
    set({ tracks, hydrated: true });
  },

  addTrack: async (track) => {
    const tracks = [...get().tracks, track];
    set({ tracks });
    await persist(tracks);
  },

  updateTrack: async (id, patch) => {
    const tracks = get().tracks.map((t) => (t.id === id ? { ...t, ...patch } : t));
    set({ tracks });
    await persist(tracks);
  },

  removeTrack: async (id) => {
    const tracks = get().tracks.filter((t) => t.id !== id);
    set({ tracks });
    await persist(tracks);
  },

  setSearchQuery: (q) => set({ searchQuery: q }),

  setSort: (key) => {
    const { sortKey, sortAsc } = get();
    if (key === sortKey) {
      set({ sortAsc: !sortAsc });
    } else {
      set({ sortKey: key, sortAsc: true });
    }
  },

  visibleTracks: () => {
    const { tracks, searchQuery, sortKey, sortAsc } = get();
    const q = searchQuery.trim().toLowerCase();
    const filtered = q
      ? tracks.filter(
          (t) =>
            t.title.toLowerCase().includes(q) ||
            t.artist.toLowerCase().includes(q) ||
            (t.genre ?? "").toLowerCase().includes(q)
        )
      : tracks;

    const sorted = [...filtered].sort((a, b) => {
      let cmp = 0;
      if (sortKey === "title") cmp = a.title.localeCompare(b.title);
      else if (sortKey === "bpm") cmp = (a.bpm ?? 0) - (b.bpm ?? 0);
      else if (sortKey === "genre") cmp = (a.genre ?? "").localeCompare(b.genre ?? "");
      else cmp = a.addedAt - b.addedAt;
      return sortAsc ? cmp : -cmp;
    });

    return sorted;
  },
}));
