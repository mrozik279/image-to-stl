# Klikacz Łapki (Dog Paw Clicker)

Prosta gra-klikacz na Androida: stukasz w łapkę psa, licznik kliknięć rośnie,
a zarobione "kości" można wydać na ulepszenia i odblokowanie nowych ras.
Aplikacja jest w pełni lokalna — cały postęp zapisuje się na urządzeniu
(`SharedPreferences`), bez logowania, reklam ani połączenia z internetem.

## Zaimplementowane funkcje

- **Licznik kliknięć** — `tapCount` rośnie o dokładnie 1 przy każdym
  fizycznym stuknięciu w łapkę i nigdy się nie zmniejsza (nawet gdy
  wydajesz walutę w sklepie). To jest ten "na pewno licznik kliknięć",
  o który prosiłeś — zawsze widoczny na górze ekranu głównego.
- **Animacja stuknięcia** — łapka odbija się (spring bounce) i unosi się
  znad niej pływający tekst `+N` pokazujący ile kości zarobiłeś tym kliknięciem.
- **Wibracje i dźwięk** — krótka wibracja i cichy dźwięk przy każdym tapnięciu
  (oba przełączalne w Ustawieniach).
- **System kombo** — 5+ kliknięć w ciągu 1,5 sekundy włącza mnożnik x2 do
  zarabianych kości; najlepsze kombo zapisywane jest w statystykach.
- **Sklep z ulepszeniami** — "Smaczniejszy przysmak" zwiększa liczbę kości
  za kliknięcie, "Pomocny szczeniak" generuje kości pasywnie (raz na sekundę),
  koszt rośnie geometrycznie z każdym poziomem.
- **Rasy psów do odblokowania** — Kundelek (start), Labrador (100 kliknięć),
  Husky (500), Corgi (2000), Jamnik (10 000) — każda ma inny kolor łapki.
- **Osiągnięcia** — 9 kamieni milowych (10 → 50 000 kliknięć) z własnymi
  nazwami, widoczne na osobnej zakładce z ikonami odblokowane/zablokowane.
- **Bonus dnia** — powrót do gry następnego dnia z rzędu daje bonusowe kości
  i buduje passę (`dailyStreak`).
- **Reset postępu** — z potwierdzeniem w dialogu, w Ustawieniach.

## Pomysły na rozwój (niezaimplementowane, do wyboru)

Kilka ciekawych kierunków, gdybyś chciał rozbudować grę dalej:

1. **Widget na ekranie głównym** pokazujący licznik na żywo i pozwalający
   klikać bez otwierania aplikacji (`AppWidgetProvider` + `RemoteViews`).
2. **Prawdziwe dźwięki** — nagranie/dodanie plików audio (szczekanie,
   chrupanie przysmaku) odtwarzanych przez `SoundPool` zamiast obecnego
   prostego tonu z `ToneGenerator`.
3. **Krytyczne kliknięcia** — losowa szansa (np. 5%) na "super klik" dający
   10x więcej kości, z efektem konfetti/błysku.
4. **Minigra "złap łapkę"** — łapka losowo pojawia się w różnych miejscach
   ekranu i znika po chwili; trzeba trafić zanim zniknie (tryb zręcznościowy,
   osobny licznik "trafień").
5. **Tabela wyników offline** — zapis najlepszych sesji (kliknięć/minutę)
   lokalnie, bez serwera.
6. **Personalizacja psa** — zdjęcie własnego psa jako tło/skórka łapki
   (import z galerii, przycinanie).
7. **Tryb ciemny** — motyw już wspiera `isSystemInDarkTheme()`, można dodać
   ręczny przełącznik w Ustawieniach.
8. **Codzienne wyzwania** — np. "kliknij 200 razy dzisiaj" z dodatkową
   nagrodą, resetowane o północy.
9. **Eksport/import zapisu** — kopia zapasowa postępu do pliku JSON, przydatne
   przy zmianie telefonu (bez chmury, ręczny transfer pliku).
10. **Osiągnięcia z odznakami graficznymi** zamiast samej ikony
    zablokowane/odblokowane — unikalna ikonka dla każdego kamienia milowego.

## Struktura projektu

```
android-dog-paw-clicker/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mrozik279/pawclicker/
│       │   ├── MainActivity.kt
│       │   ├── ClickerViewModel.kt        # logika gry, combo, ticker pasywny
│       │   ├── data/
│       │   │   ├── ClickerState.kt        # model stanu (licznik, kości, poziomy...)
│       │   │   ├── ClickerRepository.kt   # zapis/odczyt SharedPreferences
│       │   │   └── GameData.kt            # rasy, kamienie milowe, koszty ulepszeń
│       │   └── ui/
│       │       ├── PawClickerApp.kt       # Scaffold + dolna nawigacja
│       │       ├── theme/Theme.kt
│       │       └── screens/
│       │           ├── ClickerScreen.kt       # ekran główny z animacją tapnięcia
│       │           ├── ShopScreen.kt          # ulepszenia + wybór rasy
│       │           ├── AchievementsScreen.kt  # lista kamieni milowych
│       │           └── SettingsScreen.kt      # dźwięk/wibracje/statystyki/reset
│       └── res/
│           ├── values/strings.xml
│           ├── drawable/ic_paw.xml            # wektorowa łapka (tintowana per rasa)
│           └── mipmap*/ic_launcher*           # ikona aplikacji
├── build.gradle.kts
└── settings.gradle.kts
```

## Budowanie i uruchomienie

Ten kontener nie ma zainstalowanego Android SDK/emulatora, więc projekt nie
został tu skompilowany — otwórz go w Android Studio (Koala lub nowszy) albo
zbuduj z linii poleceń, mając zainstalowany Android SDK:

```bash
cd android-dog-paw-clicker
./gradlew assembleDebug          # wymaga wygenerowania gradlew/gradlew.bat
```

> Ten katalog nie zawiera pliku `gradlew` (skryptu wrappera) ani binarnego
> `gradle-wrapper.jar` — wygeneruj je lokalnie poleceniem
> `gradle wrapper --gradle-version 8.7` (wymaga zainstalowanego Gradle) albo
> po prostu otwórz folder w Android Studio, które samo dogeneruje wrapper.

Wymagania:
- Android Studio Koala+ / Gradle 8.7+ / AGP 8.5.2
- JDK 17
- `compileSdk`/`targetSdk` 34, `minSdk` 24 (Android 7.0+)

Po otwarciu w Android Studio: `Run ▶` na emulatorze lub podłączonym telefonie.

## Uwagi techniczne

- Cała logika gry działa w `ClickerViewModel` (Kotlin, Jetpack Compose,
  Material 3). Stan trzymany jest w pamięci (`mutableStateOf`) i po każdej
  zmianie asynchronicznie zapisywany do `SharedPreferences` — brak
  zewnętrznych zależności, bazy danych czy sieci.
- Pasywny dochód ("Pomocny szczeniak") nalicza się tylko gdy aplikacja jest
  aktywna na pierwszym planie (korutyna w `viewModelScope`) — nie ma
  background service ani WorkManagera, więc offline-progress nie jest
  naliczany, gdy appka jest zamknięta.
