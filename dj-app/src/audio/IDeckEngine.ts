import type { LoopRegion } from "@/types";

export interface DeckSnapshot {
  isLoaded: boolean;
  isPlaying: boolean;
  isBuffering: boolean;
  positionMillis: number;
  durationMillis: number;
  didJustFinish: boolean;
}

/**
 * Common surface mixerStore drives regardless of where the audio actually
 * comes from. Local files (expo-av) support continuous rate control;
 * YouTube's IFrame API only exposes a handful of discrete playback rates -
 * getAvailableRates() communicates that so the UI can adapt (slider vs.
 * stepped buttons) instead of pretending both engines behave the same.
 */
export interface IDeckEngine {
  onUpdate(listener: (snapshot: DeckSnapshot) => void): () => void;
  /** `source` is a local file URI for the local engine, a YouTube video ID for the YouTube engine. */
  load(source: string): Promise<number>;
  unload(): Promise<void>;
  play(): Promise<void>;
  pause(): Promise<void>;
  seek(positionMillis: number): Promise<void>;
  setVolume(volume: number): Promise<void>;
  setRate(rate: number): Promise<void>;
  setKeylock(enabled: boolean): Promise<void>;
  setLoop(loop: LoopRegion): void;
  getRate(): number;
  /** null = continuous rate control; an array = only these discrete rates are supported. */
  getAvailableRates(): number[] | null;
}
