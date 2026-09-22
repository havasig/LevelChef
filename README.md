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
- **`core:database`** — KMP: the SQLDelight schema (cooking sessions, survey response, pantry, saved recipes, badges, weekly challenges) and its migrations.
- **`domain`** — KMP: repository interfaces (`CookingSessionRepository`, `RecipeRepository`, `UserProfileRepository`) and use cases, depends only on `core:model`. Kept intentionally thin — `GetChefLevelUseCase` is the only use case, since it's the only one with real logic beyond a 1:1 repository delegation; has its own unit tests (`domain/src/commonTest`).
- **`data`** — KMP: repository implementations backed by `core:database` (SQLDelight) and a Koin DI wiring (`dataModule` / `databaseModule`); `RecipeRepositoryImpl` is currently a static sample-data stub pending the Gemini-backed recommender.
- **`core:ui`** — Compose theme (`Color`/`Theme`/`Type`). Follows the system light/dark setting; both palettes come from the Figma file (dark: `#0f0f1a` background, `#534AB7` accent). Flat/no-shadow cards, 12px radius, 0.5px borders.
- **`core:designsystem`** — reusable Compose components (`LevelChefBadge`, `LevelChefTag`, `LevelChefDivider`, `PlaceholderScreen`, ...), depends on `core:ui`.
- **`feature:home`** — fully implemented from Figma node `296:1929`: level pill + XP bar, stat cards, weekly challenge card, primary CTA, 3 recipe recommendations, last-cooked card. Wired end-to-end: `HomeRoute` (stateful) resolves a Koin-injected `HomeViewModel`, which loads real data from `domain` (profile, chef level, last-cooked session, recipe recommendations) into the stateless `HomeScreen`/`HomeUiState`. The weekly-challenge card reads `WeeklyChallengeRepository`.
- **`feature:onboarding`, `feature:settings`, `feature:ingredients`, `feature:recipedetail`, `feature:mealreview`, `feature:trophyroom`, `feature:cookinglog`** — one module per screen, all fully built out from their Figma nodes. `feature:cookinglog` is the real content of the bottom-nav **Recipes** tab (search + All/Cooked/New saved-recipes list), not a separate drill-down.
- **`androidApp`** — the application shell: `MainActivity`, bottom-nav `NavHost` wiring the Home / Recipes / Trophies tabs, Koin startup (`LevelChefApplication`, registers every module's DI module). Depends on every module above but nothing depends on it.

Dependency direction is one-way: `feature:*` → `core:designsystem`/`core:ui` (+ `domain`/`data` once a feature needs real data), `data` → `domain` → `core:model`, `core:database` is only referenced from `data`. No feature module depends on another.

## Next steps

1. Open in Android Studio, let Gradle sync (needs the Android SDK installed locally).
2. See [`AGENTS.md`](AGENTS.md)'s "Not yet done" section for the current list of open work
   (the Gemini-backed recipe recommender, the Inter font,
   the iOS app shell, and recipe-detail follow-ups) — kept there rather than duplicated here
   so it doesn't drift out of sync.
