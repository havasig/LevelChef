---
name: new-feature-screen
description: Build out one of LevelChef's stub feature screens (recipedetail, mealreview, trophyroom, cookinglog) from its Figma node into a full Compose implementation, following the project's module and screen conventions. Use whenever asked to implement, build, flesh out, or "code up" a feature screen from Figma, or to replace a PlaceholderScreen.
---

# Build a LevelChef feature screen from Figma

Several `feature:*` modules are still one-line `PlaceholderScreen` stubs with their Figma node ID
in a KDoc comment. This skill turns one into a real screen.

Read `AGENTS.md` first for the module rules and conventions this skill assumes.

## 1. Identify the target

| Module | Figma node | Screen |
|---|---|---|
| `feature:mealreview` | `385:586` | Meal Review |
| `feature:trophyroom` | `504:1026` | Trophy Room |
| `feature:cookinglog` | `489:1362` | Cooking Log |

(`feature:home`, `feature:onboarding`, `feature:settings`, `feature:ingredients` and
`feature:recipedetail` are already built — use them as reference implementations.)

Confirm the node ID from the `/** Figma node NNN:NNN */` KDoc on the current stub composable.

## 2. Pull the design

Use the Figma MCP tools (the `figma-design-to-code` skill is the MANDATORY prerequisite before `get_design_context`):

- `get_screenshot` for the node — visual reference.
- `get_design_context` for the node — layout, tokens, text, components.
- `get_variable_defs` — map Figma variables to `core.ui.theme` names.

Map every color/spacing/radius onto existing tokens in `core/ui/src/main/kotlin/com/levelchef/core/ui/theme/`
(`Color.kt`, `Theme.kt`/`LevelChefShapes`, `Type.kt`). Only add a new token if the design genuinely
introduces one, and add it to those files (not inline).

## 3. Reuse the design system

Check `core/designsystem/` for components that already cover parts of the screen
(`LevelChefBadge`, `LevelChefTag`, `LevelChefDivider`, …). If a visual element appears on this screen
*and* is likely to recur on another, build it in `core:designsystem`, not the feature module.

## 4. Present the plan, get approval

Before writing any code, present the plan for this screen (files to create/edit, design-system
additions, domain/nav changes, anything left stubbed) and wait for the user to accept it. Then
create the todo list. Re-propose if declined.

## 5. Implement, following the `feature:home` pattern

In `feature/<name>/src/main/kotlin/com/levelchef/feature/<name>/`:

- `<Name>UiState.kt` — single data class, all fields defaulted.
- `<Name>Screen.kt` — **stateless** `@Composable fun <Name>Screen(state: <Name>UiState, on…: () -> Unit)`.
- `<Name>ScreenSections.kt` — private section composables if the screen has several distinct blocks.
- `<Name>Route.kt` — **stateful** `@Composable fun <Name>Route(viewModel: <Name>ViewModel = koinViewModel())`
  that does `val state by viewModel.uiState.collectAsState()` and calls the stateless screen.
  Only add this + the ViewModel if the screen needs data; a purely static screen can skip straight to `<Name>Screen`.
- `<Name>ViewModel.kt` — `class … : ViewModel()`, `MutableStateFlow(<Name>UiState())` exposed as `asStateFlow()`,
  load in `init { refresh() }` via injected **domain** repository interfaces / use cases (never `data` impls).
- `Sample<Name>Data.kt` — sample `<Name>UiState` for `@Preview` and as a stopgap default.
- `di/<Name>Module.kt` — Koin `val <name>Module = module { viewModel { <Name>ViewModel(get(), …) } }`.

Keep all Compose imports explicit. Dark theme only — no shadows/elevation, 0.5px borders, 12px radius.

## 6. Wire dependencies

- `feature/<name>/build.gradle.kts`: add `implementation(project(":domain"))`,
  `implementation(libs.androidx.lifecycle.viewmodel.compose)`, `implementation(libs.koin.viewmodel.compose)`
  **only if** you added a ViewModel. Mirror `feature/home/build.gradle.kts`. Use `libs.…` — never raw coordinates.
- If you introduced a new domain need, add the interface to `domain` and a real (or stub) impl in `data`,
  registered in `dataModule`.
- `androidApp/.../LevelChefApplication.kt`: add `<name>Module` to `startKoin { modules(…) }`.
- `androidApp/.../ui/nav/LevelChefNav.kt`: replace the placeholder `composable(...) { <Name>Screen() }`
  with `<Name>Route(...)` and wire any nav args / click callbacks.

## 7. Verify

```bash
./gradlew :feature:<name>:assembleDebug
./gradlew :feature:<name>:testDebugUnitTest   # if you added logic worth testing
./gradlew :androidApp:assembleDebug
```

Add `commonTest`-style tests for any non-trivial ViewModel/mapper logic, using hand-written fake
repositories (see `domain/src/commonTest/.../GetChefLevelUseCaseTest.kt`). A feature module with a
ViewModel also needs `alias(libs.plugins.kover)` in its build file and `kover(project(":feature:<name>"))`
in the root `build.gradle.kts` (the 90% logic-coverage gate covers `*ViewModel` + `*DomainMappersKt`).

## 7b. Screenshot baseline

Add a screenshot test so visual changes to this screen show up as PR comments (see
`feature/home/src/test/.../HomeScreenScreenshotTest.kt` and `feature/home/build.gradle.kts`):

- module build file: `alias(libs.plugins.roborazzi)`, `testOptions { unitTests { isIncludeAndroidResources = true } }`,
  `roborazzi { outputDir.set(layout.projectDirectory.dir("src/test/screenshots")) }`, and the
  `roborazzi*` / `robolectric` / `androidx.compose.ui.test.*` `testImplementation` deps.
- `<Name>ScreenScreenshotTest.kt`: `@RunWith(RobolectricTestRunner)` + `@GraphicsMode(NATIVE)` +
  `@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")`, render `LevelChefTheme { <Name>Screen(...) }`,
  `compose.onRoot().captureRoboImage()` — one per meaningful state.
- `./gradlew :feature:<name>:recordRoborazziDebug`, then commit `feature/<name>/src/test/screenshots/*.png`.
- Add the path filter for the new module is already covered by `feature/**` in `.github/workflows/screenshots.yml`.

Once ≥2 feature modules have this, promote the shared wiring to a `levelchef.roborazzi` convention
plugin (watch for the precompiled-accessor issue that forced detekt to be root-only).

## 8. Update the manual QA script

`docs/QA_REGRESSION.md` is part of the definition of done (see `AGENTS.md` → Working practices).
In the same PR:

- Add a new `SM-NN` scenario for the screen — **Priority**, **Preconditions**, numbered **Steps**,
  a **bold expected** result per step. Cover the happy path plus any persisted state, new settings,
  and the new nav entry/back path.
- Add its row to the results-log table; extend `SM-13` (navigation) and `SM-10` (Hungarian
  spot-check) if this screen adds routes or strings; extend `SM-14` if it persists anything new.
- Bump the file's "Last updated" line (date + PR number).

Follow the checklist in that file's *Extending this script* section.

## 9. Report

Summarize: node(s) implemented, new design-system components, new domain interfaces, nav wiring changes,
the `SM-NN` scenario(s) added to `docs/QA_REGRESSION.md`, and anything left stubbed (e.g. data still
coming from `SampleData`).
