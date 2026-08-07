import { create } from "zustand";
import { Audio } from "expo-av";
import { DeckEngine } from "@/audio/DeckEngine";
import { crossfaderGains } from "@/audio/equalPower";
import { useLibraryStore } from "@/store/libraryStore";
import type { DeckId, DeckState, CuePoint } from "@/types";

const MIN_RATE = 0.92;
const MAX_RATE = 1.08;
const TAP_RESET_MS = 2000;
const TAP_MIN_SAMPLES = 3;

function emptyDeckState(): DeckState {
  return {
    trackId: null,
    isLoaded: false,
    isPlaying: false,
    isBuffering: false,
    positionMillis: 0,
    durationMillis: 0,
    rate: 1.0,
    volume: 1.0,
    loop: { active: false, inMillis: 0, outMillis: 0 },
    cues: [null, null, null, null],
    effectActive: null,
  };
}

interface MixerState {
  audioReady: boolean;
  decks: Record<DeckId, DeckState>;
  crossfader: number; // -1..1
  masterVolume: number;
  tapTimestamps: Record<DeckId, number[]>;

  initAudio: () => Promise<void>;
  loadTrack: (deck: DeckId, trackId: string) => Promise<void>;
  togglePlay: (deck: DeckId) => Promise<void>;
  seek: (deck: DeckId, positionMillis: number) => Promise<void>;
  setDeckVolume: (deck: DeckId, volume: number) => void;
  setCrossfader: (value: number) => void;
  setRate: (deck: DeckId, rate: number) => Promise<void>;
  toggleKeylock: (deck: DeckId, enabled: boolean) => Promise<void>;
  tapTempo: (deck: DeckId) => void;
  syncToOtherDeck: (deck: DeckId) => Promise<void>;
  setLoopBeats: (deck: DeckId, beats: number | null) => void;
  toggleLoop: (deck: DeckId) => void;
  setCue: (deck: DeckId, index: number) => void;
  jumpToCue: (deck: DeckId, index: number) => Promise<void>;
  triggerBrake: (deck: DeckId) => Promise<void>;
  triggerEchoOut: (deck: DeckId) => Promise<void>;
}

const engines: Record<DeckId, DeckEngine> = {
  A: new DeckEngine(),
  B: new DeckEngine(),
};

function applyDeckOutputVolume(get: () => MixerState, deck: DeckId) {
  const state = get();
  const { gainA, gainB } = crossfaderGains(state.crossfader);
  const crossGain = deck === "A" ? gainA : gainB;
  const finalVolume = state.decks[deck].volume * crossGain * state.masterVolume;
  engines[deck].setVolume(finalVolume);
}

export const useMixerStore = create<MixerState>((set, get) => {
  (["A", "B"] as DeckId[]).forEach((deck) => {
    engines[deck].onUpdate((snapshot) => {
      set((state) => ({
        decks: {
          ...state.decks,
          [deck]: {
            ...state.decks[deck],
            isLoaded: snapshot.isLoaded,
            isPlaying: snapshot.isPlaying,
            isBuffering: snapshot.isBuffering,
            positionMillis: snapshot.positionMillis,
            durationMillis: snapshot.durationMillis || state.decks[deck].durationMillis,
          },
        },
      }));
    });
  });

  return {
    audioReady: false,
    decks: { A: emptyDeckState(), B: emptyDeckState() },
    crossfader: 0,
    masterVolume: 1,
    tapTimestamps: { A: [], B: [] },

    initAudio: async () => {
      if (get().audioReady) return;
      await Audio.setAudioModeAsync({
        playsInSilentModeIOS: true,
        staysActiveInBackground: true,
        shouldDuckAndroid: false,
      });
      set({ audioReady: true });
    },

    loadTrack: async (deck, trackId) => {
      const track = useLibraryStore.getState().tracks.find((t) => t.id === trackId);
      if (!track) return;
      await get().initAudio();
      const duration = await engines[deck].load(track.uri);
      set((state) => ({
        decks: {
          ...state.decks,
          [deck]: {
            ...emptyDeckState(),
            trackId,
            isLoaded: true,
            durationMillis: duration,
            volume: state.decks[deck].volume,
          },
        },
      }));
      applyDeckOutputVolume(get, deck);
    },

    togglePlay: async (deck) => {
      const d = get().decks[deck];
      if (!d.isLoaded) return;
      if (d.isPlaying) await engines[deck].pause();
      else await engines[deck].play();
    },

    seek: async (deck, positionMillis) => {
      await engines[deck].seek(positionMillis);
    },

    setDeckVolume: (deck, volume) => {
      set((state) => ({
        decks: { ...state.decks, [deck]: { ...state.decks[deck], volume } },
      }));
      applyDeckOutputVolume(get, deck);
    },

    setCrossfader: (value) => {
      set({ crossfader: Math.min(1, Math.max(-1, value)) });
      applyDeckOutputVolume(get, "A");
      applyDeckOutputVolume(get, "B");
    },

    setRate: async (deck, rate) => {
      const clamped = Math.min(MAX_RATE, Math.max(MIN_RATE, rate));
      set((state) => ({ decks: { ...state.decks, [deck]: { ...state.decks[deck], rate: clamped } } }));
      await engines[deck].setRate(clamped);
    },

    toggleKeylock: async (deck, enabled) => {
      await engines[deck].setKeylock(enabled);
    },

    tapTempo: (deck) => {
      const now = Date.now();
      const existing = get().tapTimestamps[deck];
      const withinWindow = existing.length > 0 && now - existing[existing.length - 1] > TAP_RESET_MS;
      const taps = withinWindow ? [now] : [...existing, now];
      const trimmed = taps.slice(-8);
      set((state) => ({ tapTimestamps: { ...state.tapTimestamps, [deck]: trimmed } }));

      if (trimmed.length >= TAP_MIN_SAMPLES) {
        const intervals = trimmed.slice(1).map((t, i) => t - trimmed[i]);
        const avgMs = intervals.reduce((a, b) => a + b, 0) / intervals.length;
        const bpm = Math.round((60000 / avgMs) * 10) / 10;
        const trackId = get().decks[deck].trackId;
        if (trackId) {
          useLibraryStore.getState().updateTrack(trackId, { bpm });
        }
      }
    },

    syncToOtherDeck: async (deck) => {
      const other: DeckId = deck === "A" ? "B" : "A";
      const tracks = useLibraryStore.getState().tracks;
      const thisTrack = tracks.find((t) => t.id === get().decks[deck].trackId);
      const otherTrack = tracks.find((t) => t.id === get().decks[other].trackId);
      if (!thisTrack?.bpm || !otherTrack?.bpm) return;
      const otherEffectiveBpm = otherTrack.bpm * get().decks[other].rate;
      const targetRate = otherEffectiveBpm / thisTrack.bpm;
      await get().setRate(deck, targetRate);
    },

    setLoopBeats: (deck, beats) => {
      const state = get();
      const d = state.decks[deck];
      const trackId = d.trackId;
      const track = useLibraryStore.getState().tracks.find((t) => t.id === trackId);
      const effectiveBpm = (track?.bpm ?? 120) * d.rate;
      const beatMs = 60000 / effectiveBpm;

      if (beats === null) {
        engines[deck].setLoop({ active: false, inMillis: 0, outMillis: 0 });
        set((s) => ({
          decks: { ...s.decks, [deck]: { ...s.decks[deck], loop: { active: false, inMillis: 0, outMillis: 0 } } },
        }));
        return;
      }

      const inMillis = d.positionMillis;
      const outMillis = inMillis + beatMs * beats;
      const loop = { active: true, inMillis, outMillis };
      engines[deck].setLoop(loop);
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], loop } } }));
    },

    toggleLoop: (deck) => {
      const d = get().decks[deck];
      const loop = { ...d.loop, active: !d.loop.active };
      engines[deck].setLoop(loop);
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], loop } } }));
    },

    setCue: (deck, index) => {
      const d = get().decks[deck];
      const cue: CuePoint = { positionMillis: d.positionMillis, label: `Cue ${index + 1}` };
      const cues = [...d.cues];
      cues[index] = cue;
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], cues } } }));
    },

    jumpToCue: async (deck, index) => {
      const cue = get().decks[deck].cues[index];
      if (!cue) return;
      await engines[deck].seek(cue.positionMillis);
    },

    /**
     * Classic "power off" turntable effect: ramp playback rate down to
     * near-zero over ~1.3s, then stop and restore the original rate for
     * next play. Pure rate automation - no extra audio nodes required.
     */
    triggerBrake: async (deck) => {
      const engine = engines[deck];
      const startRate = get().decks[deck].rate;
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], effectActive: "brake" } } }));

      const steps = 12;
      const stepDurationMs = 1300 / steps;
      for (let i = 1; i <= steps; i++) {
        const t = i / steps;
        const rate = Math.max(0.02, startRate * (1 - t));
        await engine.setRate(rate);
        await new Promise((r) => setTimeout(r, stepDurationMs));
      }
      await engine.pause();
      await engine.setRate(startRate);
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], effectActive: null } } }));
    },

    /**
     * "Echo out" DJ trick: shrink the loop window down through a few
     * subdivisions while fading the deck's volume to silence, then stop.
     * Approximates a delay/echo tail using only loop + volume automation
     * (expo-av has no real-time DSP/effects graph to run true delay taps).
     */
    triggerEchoOut: async (deck) => {
      const engine = engines[deck];
      const state = get();
      const d = state.decks[deck];
      const track = useLibraryStore.getState().tracks.find((t) => t.id === d.trackId);
      const effectiveBpm = (track?.bpm ?? 120) * d.rate;
      const beatMs = 60000 / effectiveBpm;
      const startVolume = d.volume;

      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], effectActive: "echo" } } }));

      const subdivisions = [1, 0.5, 0.25, 0.125];
      const inMillis = d.positionMillis;
      for (let i = 0; i < subdivisions.length; i++) {
        const outMillis = inMillis + beatMs * subdivisions[i];
        engine.setLoop({ active: true, inMillis, outMillis });
        const fadedVolume = startVolume * (1 - (i + 1) / subdivisions.length);
        await engine.setVolume(Math.max(0, fadedVolume) * state.masterVolume);
        await new Promise((r) => setTimeout(r, beatMs * subdivisions[i]));
      }
      await engine.pause();
      engine.setLoop({ active: false, inMillis: 0, outMillis: 0 });
      applyDeckOutputVolume(get, deck);
      set((s) => ({
        decks: {
          ...s.decks,
          [deck]: { ...s.decks[deck], effectActive: null, loop: { active: false, inMillis: 0, outMillis: 0 } },
        },
      }));
    },
  };
});

export { MIN_RATE, MAX_RATE };
