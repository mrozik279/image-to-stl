export type DeckId = "A" | "B";

export interface Track {
  id: string;
  title: string;
  artist: string;
  uri: string;
  /** Beats per minute. Estimated via tap-tempo or entered manually; undefined until set. */
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
  /** Playback rate, 1.0 = original tempo. Range 0.92-1.08 (±8%, standard pitch-bend range). */
  rate: number;
  /** Per-deck fader, 0-1. Combined with crossfader gain for final output volume. */
  volume: number;
  loop: LoopRegion;
  cues: (CuePoint | null)[];
  /** Set while a brake/spinback or echo-out effect is animating, to block conflicting actions. */
  effectActive: "brake" | "spin" | "echo" | null;
}
