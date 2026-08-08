# DJ Mixer (dj-app)

Mobilna aplikacja (Expo / React Native / TypeScript) do miksowania dwóch
utworów jak w podstawowym kontrolerze DJ-skim: biblioteka utworów, dwa decki,
crossfader, BPM/beatmatching, pętle, hot cue i kilka efektów opartych o
automatyzację odtwarzania.

To osobny, samodzielny projekt w tym repozytorium — niezwiązany z pipeline'em
image-to-stl opisanym w głównym `CLAUDE.md`.

## Funkcje

- **Biblioteka / playlisty** — import plików audio z urządzenia
  (`expo-document-picker`), zapis metadanych (tytuł, artysta, BPM, gatunek,
  tonacja) w `AsyncStorage`, wyszukiwanie i sortowanie.
- **Dwa decki** — play/pauza, przewijanie, głośność, pitch ±8% z opcjonalnym
  keylockiem (zachowanie wysokości dźwięku przy zmianie tempa).
- **BPM i beatmatching** — tap-tempo (stukanie w rytm zapisuje BPM utworu) oraz
  przycisk **SYNC**, który dopasowuje tempo jednego decka do drugiego na
  podstawie zapisanego BPM.
- **Pętle (loop)** — szybkie pętle 1/2/4/8 beatów liczone z BPM utworu i
  aktualnego tempa, plus ręczny loop on/off.
- **Hot cue** — 4 punkty pamięci na deck (przytrzymaj, by ustawić; tapnij, by
  skoczyć).
- **Efekty** — `BRAKE` (efekt „wyłączania zasilania” gramofonu — płynne
  zjechanie z tempa do zera) i `ECHO OUT` (wygaszanie utworu przez malejące
  podpętle z opadającą głośnością, symulujące ogon echa).
- **Szukaj online** — wyszukiwarka Spotify (metadane: tytuł/artysta/rok) z
  dopasowaniem do YouTube (odtwarzanie) jednym przyciskiem — bez pobierania
  plików na dysk. Zobacz „Streaming: Spotify + YouTube” niżej.
- **Sugestie** — 30 utworów uznawanych za najważniejsze z dekad 1960–2019
  (5 na dekadę), same metadane; podłączasz własny plik lub dodajesz przez
  wyszukiwarkę online.

## Streaming: Spotify + YouTube (opcjonalne)

Ekran **Online** pozwala wyszukać utwór w katalogu Spotify i dodać go do
biblioteki jako deck odtwarzany przez YouTube — bez ściągania czegokolwiek.
To wymaga własnych, darmowych kluczy API (nie mogą być nigdzie „wbudowane za
Ciebie” — trzeba założyć własne konto deweloperskie). Dwa sposoby ich wpisania:

- **Bezpośrednio w aplikacji, na telefonie** (nie trzeba komputera ani
  edytora tekstu): zakładka **Online** → **„Klucze”** w prawym górnym rogu →
  formularz zapisuje je przez `AsyncStorage` (na web: `localStorage`) tylko
  na tym urządzeniu.
- **W pliku, jeśli budujesz z komputera:**
  ```bash
  cp src/config/streamingSecrets.example.ts src/config/streamingSecrets.ts
  # uzupełnij spotifyClientId / spotifyClientSecret / youtubeApiKey
  ```
  Klucze wpisane w aplikacji zawsze mają pierwszeństwo nad tym plikiem.

- **Spotify** — [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard) →
  Create app → Client ID + Client Secret. Używamy tylko przepływu Client
  Credentials (bez logowania użytkownika) do wyszukiwania katalogu. Audio
  Features (BPM/tonacja) jest best-effort — Spotify ograniczył ten endpoint
  dla nowych aplikacji w listopadzie 2024, więc może zwrócić 403; wtedy BPM
  i tak można ustawić przez TAP BPM.
- **YouTube** — [console.cloud.google.com](https://console.cloud.google.com) →
  nowy projekt → włącz „YouTube Data API v3” → Credentials → API key. Darmowy
  limit (10 000 jednostek/dzień), jedno wyszukiwanie kosztuje 100 jednostek.

**Dlaczego tak, a nie inaczej — twarde ograniczenia platform:**
- **Spotify nie pozwala** stronom trzecim na miksowanie/przetwarzanie
  surowego dźwięku — oficjalny SDK steruje wyłącznie odtwarzaczem w samej
  apce Spotify (i wymaga konta Premium), więc Spotify służy tu tylko do
  wyszukiwania metadanych, nigdy do odtwarzania.
- **Pobieranie/ekstrakcja dźwięku z YouTube łamie ich regulamin** — dlatego
  tego nie robimy. Zamiast tego używamy oficjalnego, legalnego **YouTube
  IFrame Player API** osadzonego w WebView: dwa niezależne odtwarzacze wideo
  dają prawdziwy crossfader (głośność 0–100% ciągła), ale tempo/pitch jest
  ograniczone do kilku wartości narzuconych przez YouTube (typowo
  0.25×–2×, bez zachowania wysokości dźwięku) — stąd na decku YouTube suwak
  PITCH zamienia się w rząd przycisków zamiast płynnego suwaka.
- Deck YouTube wymaga internetu i może pokazać reklamę; deck lokalny (własny
  plik) działa w pełni offline jak wcześniej.
- **Bezpieczeństwo**: klucze (czy to z formularza w aplikacji, czy z pliku
  `streamingSecrets.ts`) trafiają na urządzenie/do paczki appki bez
  szyfrowania, więc Client Secret Spotify jest teoretycznie możliwy do
  wydobycia. Do osobistego użytku to akceptowalne; do publikacji w sklepie
  appek trzeba by przenieść wymianę tokenu na mały backend.

## Uczciwe ograniczenie techniczne

`expo-av` nie daje dostępu do prawdziwego grafu DSP w czasie rzeczywistym
(brak natywnego EQ/filtra/pogłosu/prawdziwego delaya z wieloma taktami).
Efekty `BRAKE` i `ECHO OUT` są zaimplementowane uczciwie — przez automatyzację
tempa, pętli i głośności na realnym silniku odtwarzania — a nie symulowane
etykietą bez działania. Prawdziwy EQ/filtr wymagałby natywnego modułu audio
(np. `AVAudioEngine` na iOS / `AudioEffect` na Androidzie) — to naturalny
kolejny krok rozwoju, nieobjęty tym MVP.

## Uruchomienie

Wymaga Node.js 18+ oraz aplikacji **Expo Go** na telefonie (lub emulatora
Android/iOS).

```bash
cd dj-app
npm install
npm start        # otwiera Metro bundler + QR kod dla Expo Go
# albo:
npm run android
npm run ios
npm run web       # podgląd w przeglądarce (przydatne bez telefonu pod ręką;
                   # import plików i realne audio na web mają ograniczone
                   # wsparcie — docelowa platforma to iOS/Android przez Expo Go)
```

Sprawdzenie typów:

```bash
npm run typecheck
```

## Struktura projektu

```
dj-app/
  App.tsx                    # zakładki + trwały pasek WebView dla decków YouTube
  src/
    audio/
      IDeckEngine.ts          # wspólny interfejs deck-silnika (lokalny/YouTube)
      DeckEngine.ts           # opakowanie expo-av Audio.Sound per deck + pętle
      YoutubeDeckEngine.ts    # sterowanie odtwarzaczem YouTube przez WebView
      youtubeBridgeHtml.ts    # strona-host z YouTube IFrame Player API
      equalPower.ts           # krzywa crossfadera (equal-power)
    services/
      spotify.ts              # wyszukiwanie katalogu Spotify (Client Credentials)
      youtube.ts               # wyszukiwanie YouTube Data API v3
    config/
      streamingSecrets.example.ts / streamingSecrets.ts (git-ignored)
      streamingConfig.ts        # scala klucze z formularza (priorytet) i z pliku
    store/
      libraryStore.ts          # zustand: utwory, wyszukiwanie, sortowanie, AsyncStorage
      mixerStore.ts             # zustand: decki (lokalny/YouTube), crossfader, BPM sync, efekty
      streamingKeysStore.ts      # zustand: klucze wpisane w apce (AsyncStorage), edytowalne bez komputera
    screens/
      LibraryScreen.tsx, MixerScreen.tsx, SuggestionsScreen.tsx, OnlineSearchScreen.tsx
    components/
      Deck.tsx, Crossfader.tsx, TrackListItem.tsx, TrackEditModal.tsx,
      YoutubeDeckPlayerView.tsx
    data/
      curatedPlaylist.ts        # lista "najlepsze utwory 1960-2019" (metadane)
    utils/
      importTrack.ts            # import lokalny + budowanie tracków YouTube
      format.ts
    types/index.ts
```

## Znane ograniczenia / plan rozwoju

- Brak wizualizacji fali dźwiękowej (waveform) — wymagałoby dekodowania PCM
  po stronie klienta; nieplanowane w tym MVP.
- Detekcja BPM działa przez tap-tempo, nie przez automatyczną analizę pliku
  audio (analiza offline wymaga cięższej biblioteki DSP).
- Dokładność pętli jest ograniczona częstotliwością odpytywania statusu
  odtwarzacza (50 ms) — wystarczająca do praktycznego DJ-owania, ale nie
  sample-accurate.
- Prawdziwe efekty EQ/filtr/reverb wymagają natywnego silnika audio (poza
  zakresem `expo-av`).
