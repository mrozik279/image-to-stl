import { Audio, AVPlaybackStatus, AVPlaybackStatusSuccess } from "expo-av";
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
 * Wraps a single expo-av Sound instance and adds DJ-deck behavior expo-av
 * doesn't provide natively: JS-driven loop-region enforcement (checked on
 * every status tick, so loop tightness is bounded by progressUpdateIntervalMillis
 * below rather than sample-accurate) and keylock (pitch-preserving tempo change).
 */
export class DeckEngine {
  private sound: Audio.Sound | null = null;
  private loop: LoopRegion = { active: false, inMillis: 0, outMillis: 0 };
  private listeners = new Set<(snapshot: DeckSnapshot) => void>();
  private currentRate = 1.0;
  private keylock = false;
  private loopReentryGuard = false;

  onUpdate(listener: (snapshot: DeckSnapshot) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private emit(snapshot: DeckSnapshot) {
    this.listeners.forEach((l) => l(snapshot));
  }

  async load(uri: string): Promise<number> {
    await this.unload();
    const { sound, status } = await Audio.Sound.createAsync(
      { uri },
      {
        shouldPlay: false,
        volume: 1.0,
        rate: this.currentRate,
        shouldCorrectPitch: this.keylock,
        progressUpdateIntervalMillis: 50,
      },
      this.handleStatus
    );
    this.sound = sound;
    const s = status as AVPlaybackStatusSuccess;
    return s.isLoaded ? s.durationMillis ?? 0 : 0;
  }

  async unload() {
    if (this.sound) {
      await this.sound.unloadAsync();
      this.sound = null;
    }
  }

  private handleStatus = (status: AVPlaybackStatus) => {
    if (!status.isLoaded) {
      this.emit({
        isLoaded: false,
        isPlaying: false,
        isBuffering: false,
        positionMillis: 0,
        durationMillis: 0,
        didJustFinish: false,
      });
      return;
    }

    if (
      this.loop.active &&
      !this.loopReentryGuard &&
      this.loop.outMillis > this.loop.inMillis &&
      status.positionMillis >= this.loop.outMillis
    ) {
      this.loopReentryGuard = true;
      this.sound?.setPositionAsync(this.loop.inMillis).finally(() => {
        this.loopReentryGuard = false;
      });
    }

    this.emit({
      isLoaded: true,
      isPlaying: status.isPlaying,
      isBuffering: status.isBuffering,
      positionMillis: status.positionMillis,
      durationMillis: status.durationMillis ?? 0,
      didJustFinish: status.didJustFinish ?? false,
    });
  };

  async play() {
    await this.sound?.playAsync();
  }

  async pause() {
    await this.sound?.pauseAsync();
  }

  async seek(positionMillis: number) {
    await this.sound?.setPositionAsync(Math.max(0, positionMillis));
  }

  async setVolume(volume: number) {
    await this.sound?.setVolumeAsync(Math.min(1, Math.max(0, volume)));
  }

  /** rate: 0.5-2.0 in expo-av terms; UI restricts to a musical ±8% pitch-bend range. */
  async setRate(rate: number) {
    this.currentRate = rate;
    if (this.sound) {
      await this.sound.setRateAsync(rate, this.keylock);
    }
  }

  async setKeylock(enabled: boolean) {
    this.keylock = enabled;
    if (this.sound) {
      await this.sound.setRateAsync(this.currentRate, enabled);
    }
  }

  setLoop(loop: LoopRegion) {
    this.loop = loop;
  }

  getRate() {
    return this.currentRate;
  }
}
