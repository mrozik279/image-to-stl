#!/bin/bash
# Builds the OpenSCAD Playground Android APK using the AOSP-derived toolchain
# from apt (aapt, smali, zipalign, apksigner) instead of Gradle/Android SDK
# (dl.google.com is blocked by this environment's network policy).
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST_DIR="$APP_DIR/../src-playground/dist"
BUILD_DIR="$APP_DIR/build"
OUT_APK="$APP_DIR/openscad-playground.apk"

FRAMEWORK_RES=/usr/share/android-framework-res/framework-res.apk
KEYSTORE="$APP_DIR/debug.keystore"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

echo "== 1/6: Copying built web app into assets/www =="
rm -rf "$APP_DIR/assets/www"
mkdir -p "$APP_DIR/assets/www"
cp -R "$DIST_DIR"/. "$APP_DIR/assets/www/"
# Drop Monaco language services we never use (only the custom "openscad"
# language is registered) to shave a few MB off the APK.
rm -rf "$APP_DIR/assets/www/vs/language/typescript" \
       "$APP_DIR/assets/www/vs/language/css" \
       "$APP_DIR/assets/www/vs/language/html" \
       "$APP_DIR/assets/www/vs/language/json"
rm -f "$APP_DIR/assets/www"/*.map "$APP_DIR/assets/www"/vs/**/*.map 2>/dev/null || true

echo "== 2/6: Assembling classes.dex from smali sources =="
smali assemble -a 23 -o "$BUILD_DIR/classes.dex" "$APP_DIR/smali/"

echo "== 3/6: Packaging resources + assets with aapt =="
aapt package -f \
  -M "$APP_DIR/AndroidManifest.xml" \
  -S "$APP_DIR/res" \
  -A "$APP_DIR/assets" \
  -I "$FRAMEWORK_RES" \
  -F "$BUILD_DIR/app-unsigned.apk"

echo "== 4/6: Adding classes.dex to the apk =="
# Use aapt's own zip writer (not the system `zip` tool) to append classes.dex,
# so the whole archive is written by a single, self-consistent implementation.
( cd "$BUILD_DIR" && aapt add app-unsigned.apk classes.dex )

echo "== 5/6: zipalign =="
zipalign -f -p 4 "$BUILD_DIR/app-unsigned.apk" "$BUILD_DIR/app-aligned.apk"

echo "== 6/6: signing =="
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -v \
    -keystore "$KEYSTORE" \
    -alias openscadplayground \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass openscad123 -keypass openscad123 \
    -dname "CN=OpenSCAD Playground, OU=Local Build, O=Local, L=Local, S=Local, C=US"
fi

apksigner sign --ks "$KEYSTORE" --ks-pass pass:openscad123 --key-pass pass:openscad123 \
  --min-sdk-version 23 \
  --out "$OUT_APK" "$BUILD_DIR/app-aligned.apk"

echo
echo "Verifying signature..."
apksigner verify --verbose "$OUT_APK"

echo
echo "Done: $OUT_APK"
ls -la "$OUT_APK"
