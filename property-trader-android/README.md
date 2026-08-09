# Property Trader — Android prototype

A simple, local, hot-seat (pass-the-phone) board game for 2-4 players in the
style of Monopoly, built as a fully independent Android project. It does not
use the "Monopoly" name, Hasbro's board layout, or any of its property/card
names — the board, street names, and theme here are original.

This project is **not** part of the image-to-STL pipeline. It happens to
live in this repository (a request made in this repo's session) but shares
no code, module, or `applicationId` with `android-app/` (the STL companion
app). Treat it as a separate product.

```
property-trader-android/
├── core/   pure-Kotlin/JVM module: board, players, game state, turn engine. No Android dependency.
└── app/    Android app: Jetpack Compose UI (setup screen, board, game over).
```

## What's verified, what isn't

**`:core` is unit-tested and all 31 tests pass** (board layout, dice,
movement/Go bonus, purchase/rent — including full-color-group doubling,
station and utility rent scaling — jail (bail, doubles, forced 3rd-attempt
release), bankruptcy and both game-over conditions). Verified in this
session with plain Gradle + a JDK. See `core/src/test/kotlin/`.

**`:app` (the Compose UI) has not been compiled.** This sandbox's egress
policy blocks `dl.google.com`, which is where the Android Gradle Plugin and
the AndroidX/Compose artifacts live — there is no way to build or emulate
an Android app here (same constraint as `android-app/`, see its README).
The UI code was written carefully against known-stable, standard Compose
Material3 APIs and mirrors patterns already used in `android-app/`, but
treat it as **unverified** until built in Android Studio.

## Building it

1. Open `property-trader-android/` in Android Studio (needs normal internet
   access to resolve AGP/AndroidX/Compose — nothing unusual, a standard
   two-module Gradle/AGP project).
2. Run the `app` configuration on a device or emulator (minSdk 26).
3. Or from a terminal with the Android SDK and JDK 17 installed:
   ```bash
   ./gradlew :core:test          # game engine unit tests
   ./gradlew :app:assembleDebug  # build the APK
   ```

## Rules implemented (v0)

- 40-space board, own theme (streets, 4 train stations, 2 utilities, 2 tax
  spaces, 6 "Event" placeholder spaces), 8 color groups.
- Roll two dice, move, pass-Go bonus.
- Landing on an unowned property/station/utility offers a buy/pass choice.
- Landing on a rival's property charges rent automatically (double rent for
  a complete color group; station rent scales with stations owned;
  utility rent = dice sum × 4 or × 10 depending on how many are owned).
- Fixed-amount tax spaces.
- "Go to Jail" space, and in jail: pay bail or try for doubles (forced bail
  + move after the 3rd failed attempt).
- Bankruptcy: no partial payment in v0 — an eliminated player's properties
  go to the creditor (private debt) or back to the bank (tax/bail debt).
- Game ends when only one player remains, or (optional) at a configurable
  round limit — highest net worth (cash + property price) wins, ties broken
  by lowest player id.

## Deliberately out of scope for v0

Houses/hotels, Chance/Community-Chest style cards (the six "Event" spaces
are placeholders with no effect yet), trading between players, auctions
when a purchase is declined, partial payment/mortgaging under bankruptcy,
save/resume, and any online/networked multiplayer.
