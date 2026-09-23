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
- **`data`** — KMP: repository implementations backed by `core:database` (SQLDelight) and a Koin DI wiring (`dataModule` / `databaseModule`); `RecipeRepositoryImpl` generates recommendations from the stored survey via the Gemini API (Ktor), caching each batch in SQLDelight and falling back to a small bundled recipe set when there's no API key, no survey yet, or Gemini/the cache are unavailable. Requires a `GEMINI_API_KEY` entry in `local.properties` to call the live API — see "Next steps" below.
- **`core:ui`** — Compose theme (`Color`/`Theme`/`Type`). Follows the system light/dark setting; both palettes come from the Figma file (dark: `#0f0f1a` background, `#534AB7` accent). Flat/no-shadow cards, 12px radius, 0.5px borders.
- **`core:designsystem`** — reusable Compose components (`LevelChefBadge`, `LevelChefTag`, `LevelChefDivider`, `PlaceholderScreen`, ...), depends on `core:ui`.
- **`feature:home`** — fully implemented from Figma node `296:1929`: level pill + XP bar, stat cards, weekly challenge card, primary CTA, recipe recommendations, last-cooked card. Wired end-to-end: `HomeRoute` (stateful) resolves a Koin-injected `HomeViewModel`, which loads real data from `domain` (profile, chef level, weekly challenge, last-cooked session, recipe recommendations) into the stateless `HomeScreen`/`HomeUiState`.
- **`feature:onboarding`, `feature:settings`, `feature:ingredients`, `feature:recipedetail`, `feature:mealreview`, `feature:trophyroom`, `feature:cookinglog`** — one module per screen, all fully built out from their Figma nodes. `feature:cookinglog` is the real content of the bottom-nav **Recipes** tab (search + All/Cooked/New saved-recipes list), not a separate drill-down.
- **`androidApp`** — the application shell: `MainActivity`, bottom-nav `NavHost` wiring the Home / Recipes / Trophies tabs, Koin startup (`LevelChefApplication`, registers every module's DI module). Depends on every module above but nothing depends on it.

Dependency direction is one-way: `feature:*` → `core:designsystem`/`core:ui` (+ `domain`/`data` once a feature needs real data), `data` → `domain` → `core:model`, `core:database` is only referenced from `data`. No feature module depends on another.

## Next steps

1. Open in Android Studio, let Gradle sync (needs the Android SDK installed locally).
2. Add `GEMINI_API_KEY=<your key>` to `local.properties` (gitignored) to enable live recipe
   recommendations — without it, `RecipeRepositoryImpl` serves its bundled fallback recipes instead.
3. See [`AGENTS.md`](AGENTS.md)'s "Not yet done" section for the current list of open work
   (just the iOS app shell) — kept there rather than duplicated here so it doesn't drift out of sync.
