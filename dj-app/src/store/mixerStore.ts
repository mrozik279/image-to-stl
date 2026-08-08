import { create } from "zustand";
import { Audio } from "expo-av";
import { DeckEngine } from "@/audio/DeckEngine";
import { youtubeEngines } from "@/audio/YoutubeDeckEngine";
import { crossfaderGains } from "@/audio/equalPower";
import { useLibraryStore } from "@/store/libraryStore";
import type { IDeckEngine } from "@/audio/IDeckEngine";
import type { DeckId, DeckState, CuePoint, TrackSource } from "@/types";

const MIN_RATE = 0.92;
const MAX_RATE = 1.08;
// YouTube's discrete rates go well beyond a musical pitch-bend range; clamp
// wide enough that engine.setRate can still snap to whatever it reports.
const YOUTUBE_MIN_RATE = 0.25;
const YOUTUBE_MAX_RATE = 2.0;
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
    availableRates: null,
    volume: 1.0,
    loop: { active: false, inMillis: 0, outMillis: 0 },
    cues: [null, null, null, null],
    effectActive: null,
    source: "local",
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

const localEngines: Record<DeckId, DeckEngine> = {
  A: new DeckEngine(),
  B: new DeckEngine(),
};

function engineOf(deck: DeckId, source: TrackSource): IDeckEngine {
  return source === "youtube" ? youtubeEngines[deck] : localEngines[deck];
}

function applyDeckOutputVolume(get: () => MixerState, deck: DeckId) {
  const state = get();
  const { gainA, gainB } = crossfaderGains(state.crossfader);
  const crossGain = deck === "A" ? gainA : gainB;
  const finalVolume = state.decks[deck].volume * crossGain * state.masterVolume;
  engineOf(deck, state.decks[deck].source).setVolume(finalVolume);
}

export const useMixerStore = create<MixerState>((set, get) => {
  (["A", "B"] as DeckId[]).forEach((deck) => {
    (["local", "youtube"] as TrackSource[]).forEach((source) => {
      engineOf(deck, source).onUpdate((snapshot) => {
        // Both engines stay subscribed at all times; only the one backing
        // this deck's current track should be allowed to write into state,
        // otherwise the idle engine's stale snapshot could clobber the live one.
        if (get().decks[deck].source !== source) return;
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

      const previousSource = get().decks[deck].source;
      if (previousSource !== track.source) {
        await engineOf(deck, previousSource).unload();
      }

      const engine = engineOf(deck, track.source);
      const sourceId = track.source === "youtube" ? track.youtubeVideoId ?? "" : track.uri ?? "";
      const duration = await engine.load(sourceId);

      set((state) => ({
        decks: {
          ...state.decks,
          [deck]: {
            ...emptyDeckState(),
            trackId,
            source: track.source,
            isLoaded: true,
            durationMillis: duration,
            volume: state.decks[deck].volume,
            availableRates: engine.getAvailableRates(),
          },
        },
      }));
      applyDeckOutputVolume(get, deck);
    },

    togglePlay: async (deck) => {
      const d = get().decks[deck];
      if (!d.isLoaded) return;
      const engine = engineOf(deck, d.source);
      if (d.isPlaying) await engine.pause();
      else await engine.play();
    },

    seek: async (deck, positionMillis) => {
      const d = get().decks[deck];
      await engineOf(deck, d.source).seek(positionMillis);
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
      const d = get().decks[deck];
      const [min, max] = d.source === "youtube" ? [YOUTUBE_MIN_RATE, YOUTUBE_MAX_RATE] : [MIN_RATE, MAX_RATE];
      const clamped = Math.min(max, Math.max(min, rate));
      const engine = engineOf(deck, d.source);
      await engine.setRate(clamped);
      // YouTube snaps to its own available rate; reflect what actually took effect.
      set((state) => ({ decks: { ...state.decks, [deck]: { ...state.decks[deck], rate: engine.getRate() } } }));
    },

    toggleKeylock: async (deck, enabled) => {
      const d = get().decks[deck];
      await engineOf(deck, d.source).setKeylock(enabled);
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
      const engine = engineOf(deck, d.source);

      if (beats === null) {
        engine.setLoop({ active: false, inMillis: 0, outMillis: 0 });
        set((s) => ({
          decks: { ...s.decks, [deck]: { ...s.decks[deck], loop: { active: false, inMillis: 0, outMillis: 0 } } },
        }));
        return;
      }

      const inMillis = d.positionMillis;
      const outMillis = inMillis + beatMs * beats;
      const loop = { active: true, inMillis, outMillis };
      engine.setLoop(loop);
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], loop } } }));
    },

    toggleLoop: (deck) => {
      const d = get().decks[deck];
      const loop = { ...d.loop, active: !d.loop.active };
      engineOf(deck, d.source).setLoop(loop);
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
      const d = get().decks[deck];
      const cue = d.cues[index];
      if (!cue) return;
      await engineOf(deck, d.source).seek(cue.positionMillis);
    },

    /**
     * Classic "power off" turntable effect: ramp playback rate down to
     * near-zero over ~1.3s, then stop and restore the original rate for
     * next play. Local decks glide continuously; YouTube decks only have a
     * handful of discrete rates, so they step down through whichever of
     * those are available instead of a smooth ramp.
     */
    triggerBrake: async (deck) => {
      const d = get().decks[deck];
      const engine = engineOf(deck, d.source);
      const startRate = d.rate;
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], effectActive: "brake" } } }));

      if (d.source === "youtube") {
        const steps = (engine.getAvailableRates() ?? [1])
          .filter((r) => r < startRate)
          .sort((a, b) => b - a);
        for (const rate of steps) {
          await engine.setRate(rate);
          await new Promise((r) => setTimeout(r, 260));
        }
      } else {
        const steps = 12;
        const stepDurationMs = 1300 / steps;
        for (let i = 1; i <= steps; i++) {
          const t = i / steps;
          const rate = Math.max(0.02, startRate * (1 - t));
          await engine.setRate(rate);
          await new Promise((r) => setTimeout(r, stepDurationMs));
        }
      }
      await engine.pause();
      await engine.setRate(startRate);
      set((s) => ({ decks: { ...s.decks, [deck]: { ...s.decks[deck], effectActive: null } } }));
    },

    /**
     * "Echo out" DJ trick: shrink the loop window down through a few
     * subdivisions while fading the deck's volume to silence, then stop.
     * Approximates a delay/echo tail using only loop + volume automation -
     * works the same way on local (expo-av) and YouTube decks since both
     * support continuous volume control.
     */
    triggerEchoOut: async (deck) => {
      const state = get();
      const d = state.decks[deck];
      const engine = engineOf(deck, d.source);
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
