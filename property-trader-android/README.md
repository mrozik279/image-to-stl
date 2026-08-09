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
└── app/    Android app: Jetpack Compose UI (setup, board, trade, build, rules, game over).
```

## Visuals

The first CI-built APK was correctly functioning but visually bare (plain
color rectangles, no labels). The board now draws each space's name, price,
and a type glyph (START / WIEZ. / PARK. / KOLEJ / USLUGA / $ / ?); dice roll
in an animated "cup" with real pip faces instead of plain text; a banner
pops up over the board for a couple of seconds on every notable event (pass
Go, rent paid, card drawn, jackpot, …); and Setup lets each player pick a
token color (tap a swatch — picking one already taken swaps it with whoever
had it). See `BoardCanvas.kt`, `DiceCup.kt`, `EventBanner.kt`.

## What's verified, what isn't

**`:core` is unit-tested and all 62 tests pass** (board layout, dice,
movement/Go bonus, purchase/rent — including full-color-group doubling,
house/hotel rent scaling, station and utility rent scaling — jail (bail,
doubles, forced 3rd-attempt release, get-out-of-jail-free cards),
bankruptcy, both game-over conditions, player-to-player trading, house/hotel
building, the Event card deck, and the doubles/Free-Parking/extra-roll bonus
mechanics). Verified in this session with plain Gradle + a JDK.
See `core/src/test/kotlin/`.

**`:app` (the Compose UI) has not been compiled locally in this session.**
This sandbox's egress policy blocks `dl.google.com`, which is where the
Android Gradle Plugin and the AndroidX/Compose artifacts live — there is no
way to build or emulate an Android app here (same constraint as
`android-app/`, see its README). The UI code was written carefully against
known-stable, standard Compose Material3 APIs and mirrors patterns already
used in `android-app/`, but treat it as unverified until it's actually built
somewhere with normal internet access — see the CI option below, which does
exactly that.

## Building it

**Easiest: let GitHub build it for you.** `.github/workflows/property-trader-build.yml`
builds `:core:test` and `:app:assembleDebug` on GitHub's runners (which
aren't network-restricted) on every push to this branch, or on demand via
the "Run workflow" button under the Actions tab. On success it publishes
`app-debug.apk` to a rolling GitHub Release tagged `property-trader-debug` —
open that release on the repo (works fine from a phone browser or the
GitHub app) and download the APK directly; Android will prompt to allow
installing from that source the first time. Every build is signed with the
same checked-in `app/debug.keystore` (fixed alias/passwords, debug-only,
never used for anything release-signed), so installing a newer APK over an
older one updates it in place instead of demanding an uninstall first.

**Locally, with the Android SDK and JDK 17 installed:**
```bash
cd property-trader-android
./gradlew :core:test          # game engine unit tests
./gradlew :app:assembleDebug  # build the APK
```
Or open `property-trader-android/` in Android Studio (Gradle sync needs
normal internet access) and run the `app` configuration on a device or
emulator (minSdk 26).

## Rules implemented

- 40-space board, own theme (streets, 4 train stations, 2 utilities, 2 tax
  spaces, 6 Event spaces), 8 color groups.
- Roll two dice, move, pass-Go bonus.
- Landing on an unowned property/station/utility offers a buy/pass choice.
- Landing on a rival's property charges rent automatically: base rent, or
  double for a complete unimproved color group, or the house/hotel rent tier
  once houses are built; station rent scales with stations owned; utility
  rent = dice sum × 4 or × 10 depending on how many are owned.
- Fixed-amount tax spaces — the payment doesn't vanish, it feeds the Free
  Parking pot (see below).
- "Go to Jail" space, and in jail: pay bail, try for doubles (forced bail +
  move after the 3rd failed attempt), or spend a "get out of jail free" card
  if you're holding one.
- Bankruptcy: no partial payment — an eliminated player's properties (and
  any houses on them) go to the creditor (private debt) or back to the bank
  (tax/bail/card debt).
- Game ends when only one player remains, or (optional) at a configurable
  round limit — highest net worth (cash + property and house/hotel value)
  wins, ties broken by lowest player id.
- **Trading:** from the board screen (when no other decision is pending),
  the current player opens the "Handel" screen, picks a counterpart, and
  builds a two-sided offer of properties + cash. Proposing it hands control
  to a modal accept/decline dialog (pass the phone) for the counterpart;
  accepting swaps everything atomically, declining or an offer that's gone
  stale (e.g. a property changed hands since it was proposed) is a no-op.
  See `GameEngine.proposeTrade` / `respondToTrade`.
- **Houses & hotels:** once a player owns every property in a color group,
  the "Buduj" screen lets them build up to 4 houses and then a hotel on each
  property in that group, at a per-group cost (50/100/150/200 for groups
  A–B/C–D/E–F/G–H). Each level has its own rent, well above the unimproved
  (even full-group) rate. No even-building rule and no bank house
  shortage in this MVP — see `GameEngine.buildHouse`.
- **Event cards ("Szansa"):** landing on an Event space draws the next card
  from a shuffled 10-card deck (collect/pay money, advance to Go, go to
  jail, get a "get out of jail free" card, or grant an extra-roll token) and
  applies it immediately. See `core/cards/EventCard.kt` and
  `GameEngine.drawEventCard`.
- **Bonus mechanics:** rolling doubles grants another roll immediately
  (three in a row sends you straight to jail without moving — the classic
  "speeding" rule); the Free Parking space pays out whatever has
  accumulated in the pot from tax/bail/card payments and resets it; an
  extra-roll token (from an Event card) can be spent from the "Dodatkowy
  rzut" button to roll again instead of ending your turn. See
  `GameEngine.rollAndMove` / `useExtraRollToken` and
  `GameState.freeParkingPot`.

## Deliberately out of scope

Auctions when a purchase is declined, partial payment/mortgaging under
bankruptcy, an even-building rule or finite bank house/hotel supply,
trading a property that already has houses on it (allowed, but the houses
just carry over to the new owner unchanged), save/resume, and any
online/networked multiplayer.
