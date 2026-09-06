# LevelChef

[![CI](https://github.com/havasig/LevelChef/actions/workflows/ci.yml/badge.svg)](https://github.com/havasig/LevelChef/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A gamified cooking tracker — KMP (Android now, iOS later), Jetpack Compose UI, SQLDelight local DB.

Contributing / branch & commit workflow: see [`CONTRIBUTING.md`](CONTRIBUTING.md). Architecture
and code conventions: see [`AGENTS.md`](AGENTS.md). Tooling, CI and automation reference:
see [`docs/TOOLING.md`](docs/TOOLING.md). Manual QA / regression test script:
see [`docs/QA_REGRESSION.md`](docs/QA_REGRESSION.md).

## Status

Generated from the [LevelChef Figma file](https://www.figma.com/design/GymJ5JNm5GDL6RqkS4X2OG/LevelChef), now split into a layered, multi-module Gradle project:

- **`build-logic`** — an included build with the project's convention plugins (`levelchef.android.library`, `levelchef.android.application`, `levelchef.android.feature`, `levelchef.kmp.library`). Every module applies one of these instead of repeating `compileSdk`/`minSdk`/`compose`/`jvmTarget` boilerplate.
- **`core:model`** — KMP: pure data models (`Recipe`, `CookingSession`, `Badge`, `ChefLevel`, ...), one type per file, no framework dependencies.
- **`core:database`** — KMP: the SQLDelight schema for cooking sessions.
- **`domain`** — KMP: repository interfaces (`CookingSessionRepository`, `RecipeRepository`, `UserProfileRepository`) and use cases, depends only on `core:model`. Kept intentionally thin — `GetChefLevelUseCase` is the only use case, since it's the only one with real logic beyond a 1:1 repository delegation; has its own unit tests (`domain/src/commonTest`).
- **`data`** — KMP: repository implementations backed by `core:database` (SQLDelight) and a Koin DI wiring (`dataModule` / `databaseModule`); `RecipeRepositoryImpl` is currently a static sample-data stub pending the Gemini-backed recommender.
- **`core:ui`** — Compose theme (`Color`/`Theme`/`Type`). Follows the system light/dark setting; both palettes come from the Figma file (dark: `#0f0f1a` background, `#534AB7` accent). Flat/no-shadow cards, 12px radius, 0.5px borders.
- **`core:designsystem`** — reusable Compose components (`LevelChefBadge`, `LevelChefTag`, `LevelChefDivider`, `PlaceholderScreen`, ...), depends on `core:ui`.
- **`feature:home`** — fully implemented from Figma node `296:1929`: level pill + XP bar, stat cards, weekly challenge card, primary CTA, 3 recipe recommendations, last-cooked card. Wired end-to-end: `HomeRoute` (stateful) resolves a Koin-injected `HomeViewModel`, which loads real data from `domain` (profile, chef level, last-cooked session, recipe recommendations) into the stateless `HomeScreen`/`HomeUiState`. Weekly-challenge fields still use `HomeUiState`'s defaults until a WeeklyChallenge repository exists.
- **`feature:recipedetail`, `feature:mealreview`, `feature:trophyroom`, `feature:cookinglog`, `feature:onboarding`** — one module per remaining screen, each currently a `PlaceholderScreen` with its Figma node ID noted, ready to be built out independently.
- **`androidApp`** — the application shell: `MainActivity`, bottom-nav `NavHost` wiring 4 tabs (Home / Recipes / Trophies / Log), Koin startup (`LevelChefApplication`, registers `databaseModule` + `dataModule` + `homeModule`). Depends on every module above but nothing depends on it.

Dependency direction is one-way: `feature:*` → `core:designsystem`/`core:ui` (+ `domain`/`data` once a feature needs real data), `data` → `domain` → `core:model`, `core:database` is only referenced from `data`. No feature module depends on another.

## Next steps

1. Open in Android Studio, let Gradle sync (needs the Android SDK installed locally).
2. Build out the remaining screens from their Figma nodes, inside their own `feature:*` module:
   - `feature:recipedetail` → `371:728`
   - `feature:mealreview` → `385:586`
   - `feature:trophyroom` → `504:1026`
   - `feature:cookinglog` → `489:1362`
   - `feature:onboarding` (4 steps: Introduction, Already made, Cooking regularity, dietary-restrictions/improve) → `296:1679`, `296:1678`, `296:1729`, `465:645`, `480:631`
3. Replace `RecipeRepositoryImpl`'s static sample list with a Gemini API-backed recommender (Ktor client already wired into `data`'s dependencies).
4. Wire up a WeeklyChallenge repository + domain model so `feature:home`'s challenge card stops using `HomeUiState`'s hardcoded defaults.
5. Add the Inter font family under `core/ui/src/main/res/font` for pixel-accurate typography (currently falls back to the system font).
