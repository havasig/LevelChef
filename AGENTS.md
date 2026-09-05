# AGENTS.md — LevelChef

Guidance for AI coding agents (Claude Code, Cursor, Codex, Copilot, …) working in this repo.
`CLAUDE.md` imports this file, so this is the single source of truth.

## What this is

LevelChef is a gamified cooking tracker. **Kotlin Multiplatform** (Android now, iOS later),
**Jetpack Compose** UI, **SQLDelight** local DB, **Koin** DI, **Ktor** client.
The UI was generated from the [LevelChef Figma file](https://www.figma.com/design/GymJ5JNm5GDL6RqkS4X2OG/LevelChef)
and split into a layered multi-module Gradle build.

## Module layout & dependency rules

```
androidApp ──▶ every module (nothing depends on androidApp)
feature:*  ──▶ core:designsystem, core:ui   (+ domain / data when the screen needs real data)
data       ──▶ domain ──▶ core:model
core:database  ◀── data only
```

- **`core:model`** — KMP, `commonMain`. Pure data classes, **one public type per file**, no framework deps.
- **`core:database`** — KMP. SQLDelight schema for cooking sessions.
- **`domain`** — KMP. Repository *interfaces* + use cases. Depends only on `core:model`. `api(project(":core:model"))`.
- **`data`** — KMP. Repository *implementations* (SQLDelight / Ktor) + Koin wiring (`dataModule`, `databaseModule`).
  `RecipeRepositoryImpl` is still a static sample-data stub pending the Gemini recommender.
- **`core:ui`** — Compose theme only (`Color`, `Theme`, `Type`). Follows the system light/dark setting; flat design.
- **`core:designsystem`** — reusable Compose components (`LevelChefBadge`, `LevelChefTag`, `PlaceholderScreen`, …). Depends on `core:ui`. **One public type per file** (like `core:model`); a component's data classes go in their own files (`LevelChefNavItem.kt`, `LevelChefListEntry.kt`).
- **`feature:*`** — one Android-library module per screen. **No feature module depends on another.**
  `feature:home` (Figma node `296:1929`) and `feature:onboarding` (the mandatory first-launch
  survey — `OnboardingGate` wraps the app's NavHost until a `SurveyResponse` is stored) are fully
  built; the rest are `PlaceholderScreen` stubs.

Hard rules (enforced by `:konsist:test` — see `konsist/src/test/kotlin/com/levelchef/konsist/`):
- Never add a `feature:* → feature:*` dependency.
- Never make `domain` or `core:model` depend on Android, Compose, Koin, Ktor or SQLDelight.
- `domain` depends only on `core:model`; `core:database` is imported from `data` only.
- `core:model` and `core:designsystem` files declare at most one public top-level type.
- Keep the dependency direction one-way. If a screen needs cross-feature navigation, wire it in `androidApp`'s NavHost.

## Working practices

- **Propose a plan before executing multi-step work, and wait for approval.** For any task beyond a
  trivial one-file change — building a screen, adding a domain feature, a cross-module refactor —
  first present the plan (files to touch, approach, trade-offs) and let the user accept or decline it.
  Only start editing once it's accepted; adjust and re-propose if declined. In Claude Code, use plan
  mode (`ExitPlanMode` presents the plan for approval).
- **Track approved work with a todo list.** Once a plan is accepted, create a todo list (Claude Code:
  the `TodoWrite` tool), keep exactly one item `in_progress`, and mark items done as you go.
- Prefer small, verifiable steps: change one module, build it, move on.
- **Git workflow** (see `CONTRIBUTING.md`): work on a short-lived `feat/…` `fix/…` `chore/…`
  branch off `main`, open a PR, squash-merge. `main` is protected. Commit subjects and PR titles
  are [Conventional Commits](https://www.conventionalcommits.org/) — `type(scope): subject`,
  lowercase, no trailing period (e.g. `feat(home): add weekly challenge card`); the
  `.githooks/commit-msg` hook and CI both enforce this.
- **End every response with a short "Prompt tips" section** — 1–3 specific bullets on how the user
  could have phrased the request for a better/faster result (missing context, ambiguity, scope,
  constraints, output format). Tie them to the actual message; if the prompt was already clear and
  complete, say so instead of inventing nitpicks.

## Conventions

- **English only** — all code, comments, commit messages, and docs. Translate any Hungarian design text.
- **Version catalog** — every dependency goes through `gradle/libs.versions.toml` (`libs.…`). No hardcoded coordinates in module build files.
- **Convention plugins** — module build files apply one of: `levelchef.android.feature`, `levelchef.android.library`, `levelchef.android.application`, `levelchef.kmp.library` (in `build-logic/`). Put shared config there, not in each module.
- **Source sets** — KMP modules (`core:model`, `core:database`, `domain`, `data`) use `src/commonMain/kotlin`; feature/`core:ui`/`core:designsystem` are Android libraries using `src/main/kotlin`.
- **Package root** — `com.levelchef.<module path>` (e.g. `com.levelchef.feature.home`, `com.levelchef.core.designsystem`).
- **Compose screen pattern** (see `feature:home`): stateless `XScreen(state, on…)` + stateful `XRoute(viewModel = koinViewModel())` that collects `uiState`. UI state is a single `XUiState` data class with sensible defaults. Section composables live in `XScreenSections.kt`.
- **Screen chrome** — each feature screen renders its **own** top app bar (`LevelChefTopAppBar{Home,Inner,Search}`) as the first child of its root layout, with `Modifier.statusBarsPadding()`. The bottom navigation bar is *not* per-screen: it lives in `androidApp`'s app-level `Scaffold` (`LevelChefNav.kt`) and shows only on the top-level destinations (Home, Recipes, Trophies). A drill-down screen (back arrow, no bottom bar) also adds `Modifier.navigationBarsPadding()`.
- **DI** — each feature that needs a ViewModel exposes a Koin `xModule` (`di/XModule.kt`) with `viewModel { … }`; register it in `LevelChefApplication.startKoin { modules(…) }`.
- **Theme** — follows the system light/dark setting (`LevelChefTheme` defaults to `isSystemInDarkTheme()`; `MainActivity` no longer forces a mode). No gradients, no shadows/elevation. 0.5px borders (`BorderDefault`), 12px card radius (`LevelChefShapes.small/medium`). Read colors through `LevelChefTheme.colors.*` (the theme-flipping semantic tokens), not the raw `core.ui.theme.Color` constants or `Color(0x…)`.
- **Tests** — `commonTest` with `kotlin("test")` (`kotlin.test.Test`, `assertEquals`). Coroutines via `runTest`; `Flow`/`StateFlow` assertions via **Turbine** (`flow.test { awaitItem() }`). Fakes are hand-written `private class Fake…Repository` implementing the domain interface (see `GetChefLevelUseCaseTest`). Test names use `snake_case_backtick_free` style: `returns_kitchen_novice_below_first_threshold`.
- **Coverage** — **Kover**, 90% line-coverage gate (`./gradlew koverVerify`) over the *logic* layer only: `com.levelchef.domain.usecase.*`, `com.levelchef.data.repository.*`, feature `*ViewModel` + `*DomainMappersKt`. Compose UI is excluded (it is covered by screenshot tests instead). Config is at the repo-root `build.gradle.kts`; Kover is applied per-module (`alias(libs.plugins.kover)`) — when a feature module gains a ViewModel, add that line to its build file **and** `kover(project(":feature:<name>"))` to the root `dependencies {}`.
- **Screenshot tests** — **Roborazzi** + **Robolectric** (JVM, no emulator). One `<Name>ScreenScreenshotTest.kt` per feature module rendering `LevelChefTheme { <Name>Screen(...) }` and calling `captureRoboImage()` (see `feature:home`). Baselines are committed under `feature/<name>/src/test/screenshots/`; regenerate with `./gradlew :feature:<name>:recordRoborazziDebug` and **review the PNG diff in the PR** — that is how a visual change is approved. The `Screenshots` GitHub workflow posts before/after/diff images as a PR comment but never blocks. Roborazzi is applied per-module (`alias(libs.plugins.roborazzi)` + the roborazzi/robolectric test deps + `testOptions { unitTests { isIncludeAndroidResources = true } }`).

## Build & test commands

```bash
./gradlew build                         # full build (compiles every module + lint + unit tests)
./gradlew :feature:home:assembleDebug   # one module
./gradlew :domain:allTests              # KMP module tests
./gradlew :domain:testDebugUnitTest     # Android-variant unit tests
./gradlew lint
./gradlew detekt                        # static analysis + ktlint + Compose lint rules (root aggregate task)
./gradlew detekt --auto-correct         # auto-fix formatting
./gradlew :konsist:test                 # architecture tests enforcing the module rules above
./gradlew koverVerify                    # fail if logic-layer line coverage < 90%
./gradlew koverHtmlReport               # build/reports/kover/html/index.html
./gradlew :feature:home:recordRoborazziDebug   # (re)generate screenshot baselines
./gradlew :feature:home:verifyRoborazziDebug   # fail if a screen no longer matches its baseline
./gradlew :androidApp:installDebug      # deploy to a connected device/emulator
```

Run `./gradlew build detekt :konsist:test koverVerify` before every push (CI runs the same). detekt config
lives in `config/detekt/detekt.yml`; it is applied only to the root project (detekt 2.0-alpha's
Gradle plugin is not compatible with Kotlin Gradle Plugin 2.0.x when combined with the Android
plugin), so there are no per-module detekt tasks — just the root `detekt`.

Requires the Android SDK locally (`local.properties` → `sdk.dir`). `compileSdk = 36`, `minSdk = 26`,
JVM target 11. JDK 17+ to run Gradle (the toolchain resolver fetches JDK 17 for `build-logic`).

## Building a screen from Figma

There are 4 stub screens left to build (`recipedetail`, `mealreview`, `trophyroom`, `cookinglog`).
Each has its Figma node ID in a `/** Figma node NNN:NNN */` KDoc on the stub composable.
Use the **`new-feature-screen`** skill (`.claude/skills/new-feature-screen/`) — it captures the full workflow.

## Not yet done (see README "Next steps")

1. Replace `RecipeRepositoryImpl`'s static list with a Gemini-API-backed recommender (Ktor already
   wired in `data`); feed it the stored `SurveyResponse` (see `SurveyRepository`).
2. Wire `feature:home` fully to domain use cases (partly done via `HomeViewModel`).
3. Add the Inter font under `core/ui/src/main/res/font` for pixel-accurate type.
4. iOS target (KMP modules are ready; no iOS app shell yet).
