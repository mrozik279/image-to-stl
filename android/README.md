# OpenSCAD Playground — Android APK (offline, native WebView shell)

A fully offline Android APK wrapping [openscad/openscad-playground](https://github.com/openscad/openscad-playground)
(browser-based OpenSCAD editor + WASM engine + Monaco editor + 3D viewer).
All assets (including the OpenSCAD WASM engine) are bundled inside the APK;
the app makes no network calls at runtime.

## Why this isn't a normal Capacitor/Gradle build

The original plan was `openscad-playground` build → wrap with
`@capacitor/core` + `@capacitor/android` → `gradle assembleDebug`. That
requires the Android SDK (platform + build-tools), which Gradle/AGP and the
Capacitor CLI fetch from `dl.google.com`. **This build environment's egress
policy hard-blocks `dl.google.com`** (and its `maven.google.com` alias, which
redirects to it) — confirmed via repeated `403` at the proxy CONNECT level,
not a transient failure. No Android SDK component (platform, build-tools,
`aapt2`, `d8`) can be downloaded here, so Gradle/AGP cannot run at all.

Rather than leave the task blocked, the APK is instead built with an
**AOSP-derived toolchain packaged in Ubuntu/Debian's own apt repositories**
(built from AOSP source, not redistributed Google binaries, so it isn't
subject to the same block):

- `aapt` (classic Android Asset Packaging Tool) + `android-framework-res`
  (`framework-res.apk`, used as the `-I` base for resource linking)
- `smali`/`baksmali` (Dalvik bytecode assembler) — used **instead of
  `dx`/`d8`**, which aren't packaged for Debian/Ubuntu (the `dx` apt package
  is an unrelated IBM visualization tool with a name collision; no `d8`
  package exists outside Google's blocked distribution). The native
  `MainActivity` is small enough (~100 lines: construct a `WebView`,
  configure `WebSettings`, `loadUrl`) that it's hand-written directly in
  Smali and assembled to `classes.dex` with `smali assemble`, sidestepping
  the need for a Java-bytecode dexer entirely.
- `zipalign`, `apksigner` (apt `android-sdk-build-tools`-adjacent packages)

This produces a real, correctly zipaligned and v1/v2/v3-signed APK, built
and verified entirely from open-source tooling with no dependency on the
blocked host. It does mean the native shell is a plain
`android.webkit.WebView` instead of a Capacitor bridge — that's fine here
since the app needs no native plugins, just a JS/WASM-capable local WebView.

The OpenSCAD **WASM engine** has the same problem: `openscad-playground`'s
own build (`npm run build:libs:wasm`) downloads a prebuilt binary from
`files.openscad.org`, also blocked by the same policy. Building it from
source (`openscad/openscad-wasm`, via Docker + Emscripten) is what the
upstream repo falls back to, but is a multi-hour C++ cross-compile. Instead,
the build uses the equivalent prebuilt engine published to npm as
[`openscad-wasm-prebuilt`](https://www.npmjs.com/package/openscad-wasm-prebuilt)
(registry.npmjs.org is not blocked), patched to expose the classic Emscripten
factory function the playground's worker code expects (see
`patches/openscad-wasm-default-export.patch` below) — verified to support
`--backend=manifold` and binary STL export, i.e. everything the playground
needs.

## Repo layout

```
android/
  README.md                 - this file
  patches/                  - source patches applied to the openscad-playground checkout
  openscad-playground-apk/
    app/                    - the native Android shell (committed)
      AndroidManifest.xml
      smali/                - hand-written MainActivity (WebView host), no javac/dx needed
      res/mipmap-*/          - launcher icon (from the playground's own logo512.png)
      build.sh               - aapt + smali + zipalign + apksigner pipeline -> .apk
    puppeteer-test/          - headless Chromium smoke test (committed: test.mjs only)
    src-playground/          - vendored clone of openscad/openscad-playground (gitignored, see below)
```

`src-playground/`, `puppeteer-test/node_modules/`, `app/build/`,
`app/assets/www/`, `app/debug.keystore` and the final `.apk` are gitignored:
they're either a full reproducible clone of a public upstream repo, npm
dependency trees, or generated build output — not source, per this repo's
convention of keeping generated artifacts out of git
(see root `CLAUDE.md`).

## Reproducing the build end to end

```bash
# 1. Clone upstream and apply the offline-Monaco patch
git clone https://github.com/openscad/openscad-playground.git android/openscad-playground-apk/src-playground
cd android/openscad-playground-apk/src-playground
git apply ../../patches/offline-monaco-and-wasm.patch
npm install

# 2. Get the OpenSCAD WASM engine (files.openscad.org is blocked; use the npm mirror instead)
mkdir -p libs/openscad-wasm
curl -sSL "https://registry.npmjs.org/openscad-wasm-prebuilt/-/openscad-wasm-prebuilt-1.2.0.tgz" -o /tmp/oswp.tgz
tar xzf /tmp/oswp.tgz -C /tmp oswp/package/dist/openscad.js --strip-components=3 2>/dev/null || \
  (mkdir -p /tmp/oswp && tar xzf /tmp/oswp.tgz -C /tmp/oswp)
cp /tmp/oswp/package/dist/openscad.js libs/openscad-wasm/openscad.js
cat >> libs/openscad-wasm/openscad.js << 'EOF'

async function __defaultFactory(options = {}) {
  const wasmBinary = await wasm();
  const module = {
    ...options,
    instantiateWasm(imports, receiveInstance) {
      WebAssembly.instantiate(wasmBinary, imports).then(receiveInstance);
    },
  };
  return await OpenSCAD(module);
}
export { __defaultFactory as default };
EOF
printf '\x00\x61\x73\x6d\x01\x00\x00\x00' > libs/openscad-wasm/openscad.wasm  # unused placeholder (wasm is inlined as base64 above)

# 3. Build libraries/fonts (the wasm dir already exists so this step's own
#    wasm download is skipped) and the production bundle
npm run build:libs
NODE_ENV=production npx webpack --mode=production
rm -f dist/*.map

# 4. Build the APK
cd ../app
bash build.sh
# -> android/openscad-playground-apk/app/openscad-playground.apk
```

## Testing performed (headless Chromium / Puppeteer)

`puppeteer-test/test.mjs` serves the production `dist/` locally and drives
it in headless Chromium, once with a normal desktop UA (Monaco editor path)
and once spoofing an Android WebView UA (the playground disables Monaco on
Android/iOS and falls back to a plain `<textarea>` — exactly the code path
that runs inside the real APK). Both runs: load the app, edit the model,
trigger preview (F5) and full render (F6), and assert zero console
errors/failed requests. Both passed clean with the WASM engine producing
real geometry (manifold backend, binary STL export) and the 3D viewer
rendering it. See `puppeteer-test/screenshot-desktop.png` and
`screenshot-android.png` (not committed — regenerate by running the test).

What this *doesn't* verify: actually installing/running the APK on a real
device or emulator (no Android runtime is available in this environment
either, for the same SDK-download reason). The native shell (`MainActivity`)
was validated by round-tripping `classes.dex` through `baksmali` and by a
full `aapt package` → `zipalign` → `apksigner sign` → `apksigner verify`
dry run, which succeeded. Installing on a real device is the one remaining
manual verification step.

## Known limitations

- `targetSdkVersion`/`compileSdkVersion` are pinned to 23 (Android 6.0),
  because Debian/Ubuntu only packages an `android.jar` for API 23
  (`libandroid-23-java`). This is only the *compile-time* API surface for
  the tiny native shell (a `WebView` + `WebSettings` calls, all present
  since API 1/16) — the actual JS engine used at runtime is the device's
  installed System WebView component, which auto-updates independently of
  the app's target SDK, so this doesn't limit the app's actual
  capabilities. It does mean the APK targets an old SDK floor; recent
  Android versions have been raising the *minimum installable*
  `targetSdkVersion` (Android 14 requires ≥23), so this should still install
  today, but a future OS bump could require rebuilding with a newer
  `android.jar` — see "If you need a newer target SDK" below.
- Signed with a throwaway local debug key (`app/debug.keystore`, generated
  by `build.sh` on first run, gitignored). Fine for sideloading; re-sign
  with your own release key for distribution.

## If you need a newer target SDK / real Gradle build later

If `dl.google.com` becomes reachable in some future environment (or you run
this on a machine with normal internet access), the straightforward path is
back to the originally-planned Capacitor flow: `npx cap init` + `npx cap add
android` against the `dist/` produced above, then a normal
`./gradlew assembleDebug`. Nothing in the web app build (steps 1-3 above) is
specific to this manual toolchain — only `app/` (the hand-built native
shell) would be replaced by Capacitor's generated Android project.
