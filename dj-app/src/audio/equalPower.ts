/**
 * Equal-power crossfade curve so perceived loudness stays roughly constant
 * across the fader travel (a linear crossfade dips in the middle).
 * x: -1 (full A) .. 0 (center) .. 1 (full B)
 */
export function crossfaderGains(x: number): { gainA: number; gainB: number } {
  const clamped = Math.min(1, Math.max(-1, x));
  const theta = ((clamped + 1) / 2) * (Math.PI / 2);
  return { gainA: Math.cos(theta), gainB: Math.sin(theta) };
}
