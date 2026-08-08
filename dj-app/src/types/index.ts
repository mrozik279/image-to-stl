export type DeckId = "A" | "B";

export type TrackSource = "local" | "youtube";

export interface Track {
  id: string;
  title: string;
  artist: string;
  source: TrackSource;
  /** Local file URI (sandboxed copy). Set when source === "local". */
  uri?: string;
  /** YouTube video ID, used as the streaming source when source === "youtube". */
  youtubeVideoId?: string;
  /** Beats per minute. From tap-tempo, Spotify audio-features, or manual entry; undefined until set. */
  bpm?: number;
  genre?: string;
  /** Musical key in Camelot notation, e.g. "8A". Optional, manual entry only. */
  key?: string;
  durationMillis?: number;
  addedAt: number;
}

export interface CuePoint {
  positionMillis: number;
  label: string;
}

export interface LoopRegion {
  active: boolean;
  inMillis: number;
  outMillis: number;
}

export type TrackSortKey = "addedAt" | "title" | "bpm" | "genre";

export interface DeckState {
  trackId: string | null;
  isLoaded: boolean;
  isPlaying: boolean;
  isBuffering: boolean;
  positionMillis: number;
  durationMillis: number;
  /** Playback rate, 1.0 = original tempo. Local decks: continuous ±8%. YouTube decks: snapped to availableRates. */
  rate: number;
  /** null = continuous rate control (local engine); an array = only these discrete rates work (YouTube). */
  availableRates: number[] | null;
  /** Per-deck fader, 0-1. Combined with crossfader gain for final output volume. */
  volume: number;
  loop: LoopRegion;
  cues: (CuePoint | null)[];
  /** Set while a brake/spinback or echo-out effect is animating, to block conflicting actions. */
  effectActive: "brake" | "spin" | "echo" | null;
  source: TrackSource;
}
