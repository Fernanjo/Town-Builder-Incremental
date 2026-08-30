# Cosy Town Builder

A cosy, idle-first town-building game for Android. Buildings generate resources over time
(including while the app is closed), which feed two growth tracks — **Population** and
**Technology** — that multiply production. When growth plateaus, **Prestige** resets the run in
exchange for permanent **Legacy Points** upgrades.

## Project structure

```
engine/   Pure Kotlin/JVM module: GameState, building/legacy-upgrade definitions, GameEngine
          (tick simulation), PrestigeCalculator. No Android dependency, fully unit tested.
app/      Android/Compose module: DataStore persistence, GameViewModel (tick loop + offline
          catch-up), and the four screens (Town, Build, Prestige, Legacy Shop).
```

Simulation logic lives entirely in `:engine` and is exercised by `engine/src/test`. The `:app`
module is a thin shell: it persists `GameState`, drives the 1s ticker, and renders it. Building
visuals are all in one place (`ui/components/BuildingVisual.kt`) so the placeholder Canvas shapes
can be swapped for real art later without touching game logic.

## Building

Open the project root in Android Studio (Iguana or newer) and let it sync — it targets
compileSdk/targetSdk 34, minSdk 26, AGP 8.5.2, Kotlin 2.0.21. `./gradlew assembleDebug` works from
the command line once the Android SDK is installed.

> This project was scaffolded in a sandbox without an Android SDK, so the `:app` module's build
> could not be run end-to-end here. The `:engine` module's logic was fully compiled and unit
> tested in isolation (`./gradlew :engine:test`, 11/11 passing) since it has no Android
> dependency. Please run a full `./gradlew assembleDebug` locally / in CI before relying on the
> `:app` module.

## Design decisions baked in (flagged for review/tuning)

- **Legacy Points formula:** `floor(sqrt(peakPopulation * peakTech) / 5)` — geometric mean, so a
  lopsided run earns less than a balanced one. See `PrestigeCalculator`.
- **Prestige unlock:** peak Population ≥ 50 and peak Tech Points ≥ 30 in a single run.
- **Offline catch-up cap:** 8 hours (`GameViewModel.MAX_OFFLINE_CATCH_UP_SECONDS`).
- **Building costs/output rates:** see `BuildingDefs.kt` — conservative vertical-slice defaults,
  expected to need tuning after playtesting.
