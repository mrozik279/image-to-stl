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
```

Sprawdzenie typów:

```bash
npm run typecheck
```

## Struktura projektu

```
dj-app/
  App.tsx                  # zakładki Biblioteka / Mikser
  src/
    audio/
      DeckEngine.ts         # opakowanie expo-av Audio.Sound per deck + pętle
      equalPower.ts          # krzywa crossfadera (equal-power)
    store/
      libraryStore.ts        # zustand: utwory, wyszukiwanie, sortowanie, AsyncStorage
      mixerStore.ts           # zustand: decki, crossfader, BPM sync, efekty
    screens/
      LibraryScreen.tsx
      MixerScreen.tsx
    components/
      Deck.tsx, Crossfader.tsx, TrackListItem.tsx, TrackEditModal.tsx
    utils/
      importTrack.ts          # import + kopiowanie plików do sandboxa appki
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
