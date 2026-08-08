import type WebView from "react-native-webview";
import type { LoopRegion } from "@/types";
import type { DeckSnapshot, IDeckEngine } from "@/audio/IDeckEngine";
import type { DeckId } from "@/types";

/**
 * Drives one YouTube IFrame player living inside a WebView (see
 * youtubeBridgeHtml.ts). Unlike the local expo-av engine, most calls are
 * fire-and-forget JS injected into the WebView - state comes back
 * asynchronously through handleBridgeMessage, fed by the WebView's onMessage.
 */
export class YoutubeDeckEngine implements IDeckEngine {
  private webview: WebView | null = null;
  private listeners = new Set<(snapshot: DeckSnapshot) => void>();
  private loop: LoopRegion = { active: false, inMillis: 0, outMillis: 0 };
  private loopReentryGuard = false;
  private currentRate = 1;
  private availableRates: number[] = [1];
  private pendingVideoId: string | null = null;
  private snapshot: DeckSnapshot = {
    isLoaded: false,
    isPlaying: false,
    isBuffering: false,
    positionMillis: 0,
    durationMillis: 0,
    didJustFinish: false,
  };

  attachWebView(webview: WebView | null) {
    this.webview = webview;
    if (webview && this.pendingVideoId) {
      const videoId = this.pendingVideoId;
      this.pendingVideoId = null;
      // The WebView may not have finished executing its inline script yet
      // (or even started loading) the instant it mounts, so __bridge_loadVideo
      // might not exist - fall back to a bare global the page's own onReady
      // handler also checks, covering every possible load-order race.
      webview.injectJavaScript(
        `(function(){ if (window.__bridge_loadVideo) { window.__bridge_loadVideo(${JSON.stringify(videoId)}); } else { window.__pendingBridgeVideoId = ${JSON.stringify(videoId)}; } })(); true;`
      );
    }
  }

  private inject(js: string) {
    this.webview?.injectJavaScript(`${js}; true;`);
  }

  onUpdate(listener: (snapshot: DeckSnapshot) => void): () => void {
    // Deliberately no eager listener(this.snapshot) call here: this runs
    // synchronously inside mixerStore's create() factory, before zustand has
    // an initial state to hand back from get() - firing immediately would
    // crash the very first subscription with "Cannot read properties of
    // undefined (reading 'decks')".
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private emit(patch: Partial<DeckSnapshot>) {
    this.snapshot = { ...this.snapshot, ...patch };
    this.listeners.forEach((l) => l(this.snapshot));
  }

  async load(videoId: string): Promise<number> {
    if (this.webview) {
      this.inject(`window.__bridge_loadVideo(${JSON.stringify(videoId)})`);
    } else {
      this.pendingVideoId = videoId;
    }
    this.currentRate = 1;
    this.availableRates = [1];
    this.emit({ isLoaded: true, isPlaying: false, isBuffering: false, positionMillis: 0, durationMillis: 0, didJustFinish: false });
    return 0; // real duration arrives asynchronously via the 'duration' bridge message
  }

  async unload() {
    this.inject("window.__bridge_pause()");
    this.emit({ isLoaded: false, isPlaying: false });
  }

  async play() {
    this.inject("window.__bridge_play()");
  }

  async pause() {
    this.inject("window.__bridge_pause()");
  }

  async seek(positionMillis: number) {
    this.inject(`window.__bridge_seek(${Math.max(0, positionMillis)})`);
  }

  async setVolume(volume: number) {
    const pct = Math.round(Math.min(1, Math.max(0, volume)) * 100);
    this.inject(`window.__bridge_setVolume(${pct})`);
  }

  /** Snaps to the nearest rate YouTube actually reported as available for this video. */
  async setRate(rate: number) {
    const snapped = this.snapToAvailable(rate);
    this.currentRate = snapped;
    this.inject(`window.__bridge_setRate(${snapped})`);
  }

  /** No-op: YouTube's HTML5 playback rate always shifts pitch, there's no keylock to toggle. */
  async setKeylock(_enabled: boolean) {}

  setLoop(loop: LoopRegion) {
    this.loop = loop;
  }

  getRate() {
    return this.currentRate;
  }

  getAvailableRates() {
    return this.availableRates;
  }

  private snapToAvailable(rate: number): number {
    if (!this.availableRates.length) return rate;
    return this.availableRates.reduce((best, r) => (Math.abs(r - rate) < Math.abs(best - rate) ? r : best), this.availableRates[0]);
  }

  handleBridgeMessage(raw: string) {
    let msg: any;
    try {
      msg = JSON.parse(raw);
    } catch {
      return;
    }

    switch (msg.type) {
      case "rates":
        if (Array.isArray(msg.rates) && msg.rates.length > 0) this.availableRates = msg.rates;
        break;
      case "duration":
        this.emit({ durationMillis: msg.durationMillis });
        break;
      case "stateChange":
        this.emit({
          isLoaded: true,
          isPlaying: msg.state === "playing",
          isBuffering: msg.state === "buffering",
          didJustFinish: msg.state === "ended",
        });
        break;
      case "tick": {
        if (
          this.loop.active &&
          !this.loopReentryGuard &&
          this.loop.outMillis > this.loop.inMillis &&
          msg.positionMillis >= this.loop.outMillis
        ) {
          this.loopReentryGuard = true;
          this.inject(`window.__bridge_seek(${this.loop.inMillis})`);
          setTimeout(() => {
            this.loopReentryGuard = false;
          }, 300);
        }
        this.emit({
          isLoaded: true,
          positionMillis: msg.positionMillis,
          durationMillis: msg.durationMillis || this.snapshot.durationMillis,
        });
        break;
      }
      default:
        break;
    }
  }
}

export const youtubeEngines: Record<DeckId, YoutubeDeckEngine> = {
  A: new YoutubeDeckEngine(),
  B: new YoutubeDeckEngine(),
};
