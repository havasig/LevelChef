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
  Note `Ingredient` is the pantry entity (id, category, emoji, macros, nullable `imageUrl` for a
  future AI image); `RecipeIngredient` is the line item inside `Recipe.ingredients` (name +
  optional `quantity`/`unit` so the recipe-detail servings stepper can scale it); `RecipeStep` is
  a `Recipe.steps` entry (text + optional `timerMinutes`). Ingredient imagery is the `emoji`
  field — no image library / `res/drawable` in the project.
- **`core:database`** — KMP. SQLDelight schema (**v1**, no migrations): `cookingSession`,
  `surveyResponse`, `ingredient` (+ the one-row `ingredientSeed` flag), `savedRecipe` (bookmarked
  recipe ids), `badgeEarned`, `weeklyChallengeProgress`, `generatedRecipe` (every Gemini-generated
  recipe ever shown, cached as JSON — never deleted, so a saved/cooked recipe stays resolvable even
  after a newer recommendation batch replaces it). **Pre-release:** nothing is installed
  outside development, so edit the `.sq` files directly and clear app data on dev devices — don't
  add migrations. **From the first release on**, every schema change ships a
  `migrations/N.sqm` file in the same PR (the version is the migration count + 1; no data in
  migrations), plus a `data` `androidUnitTest` that upgrades a hand-built old database. What SQL
  can't express goes in an `AfterVersion` callback passed to `AndroidSqliteDriver.Callback`.
  SQLDelight reads `SELECT *` rows by column position, so a migrated table's column order must
  match its `.sq` `CREATE` (an `ALTER TABLE … ADD COLUMN` column must be last in the `.sq`).
- **`domain`** — KMP. Repository *interfaces* + use cases. Depends only on `core:model`. `api(project(":core:model"))`.
- **`data`** — KMP. Repository *implementations* (SQLDelight / Ktor) + Koin wiring (`dataModule`, `databaseModule`).
  SQLDelight's `execute`/`executeAsOne` calls block, so every DB-backed repository wraps them in
  `withContext(dispatcher)`; `databaseModule` passes `Dispatchers.IO`. Anything bucketed by hour,
  day or week (badges, streaks, weekly challenges) reads dates in the **device time zone** via an
  injected `timeZone` provider (tests pass a fixed zone); challenge weeks run Monday to Sunday.
  The starter pantry (`DEFAULT_INGREDIENTS`) doesn't count as ingredients tried or toward badges.
  `RecipeRepositoryImpl` builds a prompt from the stored `SurveyResponse` (see `SurveyRepository`)
  and the current app language (an injected `languageTag` provider — `data` can't depend on
  `feature:settings`, so `databaseModule` reads `AppCompatDelegate.getApplicationLocales()`
  directly), calls the Gemini API (structured JSON output), and caches the result in
  `generatedRecipe`; a repeat call with an unchanged survey *and* language is served from the
  cache instead of calling Gemini again. A blank `GEMINI_API_KEY` (see root `README.md`), no
  survey yet, or any Gemini/cache failure falls back to a small bundled recipe set — English or
  Hungarian, matching the app language — recommendations are never empty. `SavedRecipeRepositoryImpl`
  backs the recipe-detail "Save" bookmark.
- **`core:ui`** — Compose Multiplatform theme only (`Color`, `Theme`, `Type`). Follows the system light/dark setting; flat design.
- **`core:designsystem`** — reusable Compose Multiplatform components (`LevelChefBadge`, `LevelChefTag`, `LevelChefRecipeCard`, …). Their built-in text (screen-reader labels, status chips, default button labels) comes from `core:designsystem`'s own `commonMain/composeResources/values{,-hu}/strings.xml`. Depends on `core:ui`. **One public type per file** (like `core:model`); a component's data classes go in their own files (`LevelChefNavItem.kt`, `LevelChefListEntry.kt`).
- **`feature:*`** — one Android-library module per screen, **all fully built** from their Figma
  nodes. **No feature module depends on another.**
  `feature:home` (Figma node `296:1929`); `feature:onboarding` (the mandatory first-launch survey —
  `OnboardingGate` wraps the app's NavHost until a `SurveyResponse` is stored); `feature:settings`
  (theme / language / retake-onboarding, reached from the Home gear); `feature:ingredients` (the
  pantry: list / detail / add-edit / delete, Figma nodes `437:1054` / `453:1520` / `447:1484`,
  reached from the Home "Ingredients tried" card); `feature:recipedetail` (Figma node `371:728` —
  data-driven recipe detail: servings stepper, ingredient checklist, numbered steps, "Save"
  bookmark, "I made it"; reached by tapping a Home recommendation card, `recipeDetail/{recipeId}`);
  `feature:mealreview` (Figma node `385:586` — "Log experience": rate a just-cooked recipe, add a
  note, adjust cook time/macros, then Save records the `CookingSession`; reached from recipe
  detail's "I made it", `mealReview/{recipeId}`, pops back to Home on save); `feature:trophyroom`
  (Figma node `504:1026` — chef-level header, weekly-challenge/kitchen-time stats, streaks and
  badges; the Trophies bottom-nav tab); `feature:cookinglog` (Figma node `489:1362` — "My saved
  recipes": search + an All/Cooked/New filter over saved recipes; this **is** the `Recipes`
  bottom-nav tab's real content, not a separate drill-down screen). `HomeRoute` refreshes its
  stats on `ON_RESUME` so a logged cook shows up when you return.

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
- **Keep the manual QA script current.** Any change that adds or alters user-visible behaviour
  (a screen, a flow, a setting, a nav path, persisted data, user-facing strings) updates
  `docs/QA_REGRESSION.md` **in the same PR** — add or edit the relevant `SM-NN` scenario and its
  results-log row, and bump the file's "Last updated" line. That file's
  [Extending this script](docs/QA_REGRESSION.md#extending-this-script) section has the checklist.
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
- **Source sets** — every module (`core:model`, `core:database`, `domain`, `data`, `core:ui`/`core:designsystem`, and every `feature:*`) is now KMP, using `src/commonMain/kotlin` (+ `src/androidMain`/`src/iosMain`/`src/androidUnitTest` for the few things that are genuinely platform-specific, such as Robolectric/Roborazzi screenshot tests, or — `feature:settings` only — real platform APIs like `AppCompatDelegate`/`SharedPreferences` with no multiplatform equivalent). `feature:*` modules that need a platform binding for something like this follow `data`'s `databaseModule` pattern: the same top-level `val`/binding name declared once per source set (`androidMain`, `iosMain`), not an `expect`/`actual` class — Gradle links whichever source set matches the compile target, so a pure-Android consumer like `androidApp` needs no changes to keep resolving the Android one.
- **Package root** — `com.levelchef.<module path>` (e.g. `com.levelchef.feature.home`, `com.levelchef.core.designsystem`).
- **Compose screen pattern** (see `feature:home`): stateless `XScreen(state, on…)` + stateful `XRoute(viewModel = koinViewModel())` that collects `uiState`. UI state is a single `XUiState` data class with sensible defaults. Section composables live in `XScreenSections.kt`.
- **Screen chrome** — each feature screen renders its **own** top app bar (`LevelChefTopAppBar{Home,Inner,Search}`) as the first child of its root layout, with `Modifier.statusBarsPadding()`. The bottom navigation bar is *not* per-screen: it lives in `androidApp`'s app-level `Scaffold` (`LevelChefNav.kt`) and shows only on the top-level destinations (Home, Recipes, Trophies). A drill-down screen (back arrow, no bottom bar) also adds `Modifier.navigationBarsPadding()`.
- **DI** — each feature that needs a ViewModel exposes a Koin `xModule` (`di/XModule.kt`) with `viewModel { … }`; register it in `LevelChefApplication.startKoin { modules(…) }`.
- **Theme** — `LevelChefTheme` reads `isSystemInDarkTheme()`. The in-app Settings theme choice
  (System/Light/Dark) is applied by `AppCompatDelegate.setDefaultNightMode()` — `MainActivity` is an
  `AppCompatActivity` so that override flows into `isSystemInDarkTheme()`; no `darkTheme` arg is
  passed. The chosen `ThemeMode` is persisted in `SharedPreferences` by `feature:settings`'
  `AppSettingsController` (app-shell config — deliberately *not* in the domain/SQLDelight layer) and
  re-applied in `LevelChefApplication.onCreate()`. No gradients, no shadows/elevation. 0.5px borders
  (`BorderDefault`), 12px card radius (`LevelChefShapes.small/medium`). Read colors through
  `LevelChefTheme.colors.*` (the theme-flipping semantic tokens), not the raw `core.ui.theme.Color`
  constants or `Color(0x…)`.
- **Language** — in-app language switching goes through `AppCompatDelegate.setApplicationLocales()`
  (AppCompat persists it via the `AppLocalesMetadataHolderService` + `autoStoreLocales` manifest
  entry); it also drives the OS per-app-language screen (`generateLocaleConfig = true`). **Every
  user-facing string is a resource with a `values-hu` twin.** `domain`/`data` can't read Android
  resources, so UI state carries the domain value (an enum like `ChefLevel`/`Difficulty`, a
  catalog id, a number of days/minutes) and the screen resolves it with `stringResource` in a
  `@Composable` label helper (e.g. `HomeLabels.kt`, `TrophyRoomLabels.kt`). English names in the
  `data` badge/challenge catalogs are only fallbacks for unknown ids.
- **Logging & errors** — **Kermit** (`co.touchlab.kermit.Logger`) is the logger, on every module's
  classpath via the convention plugins. `LevelChefApplication.onCreate()` sets the `"LevelChef"` tag,
  installs `platformLogWriter()` + `CrashLogWriter` (the WARN+ crash-reporting seam — no SDK wired
  yet), drops below `Severity.Warn` in release, then installs a global uncaught-exception handler
  (`androidApp/.../logging/`) and gives `appScope` a `CoroutineExceptionHandler`. Both log via
  Kermit, then let the app crash to the OS — there is no crash screen. Modules generally don't
  `try/catch`; a repository throw is expected to propagate to one of those handlers.
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

No stub screens remain — every `feature:*` module is fully built (see the module list above).
If a new screen is added to the Figma file, give its stub composable a
`/** Figma node NNN:NNN */` KDoc and use the **`new-feature-screen`** skill
(`.claude/skills/new-feature-screen/`), which captures the full workflow.

## Not yet done (see README "Next steps")

1. **iOS target, via Compose Multiplatform — in progress.** The build-logic/version-catalog
   foundation has landed (`levelchef.kmp.library` now declares `iosArm64`/`iosSimulatorArm64`
   alongside `androidTarget` — no `iosX64`, dropped when bumping Compose Multiplatform past `1.7.3`
   (see the "Material3 on iOS" note below); every real dev machine and this repo's own CI are Apple
   Silicon anyway, so nothing is lost; a new `levelchef.kmp.feature` convention plugin applies
   `org.jetbrains.compose` `1.11.0`, Kotlin pinned at 2.4.10 — see the "Material3 on iOS" note below
   for why), and so has a real iOS target
   for the logic layer: `core:model`/`core:database`/`domain` needed no changes at all (no
   `expect`/`actual` coupling anywhere yet), and `data` now has an `iosMain` `databaseModule`
   mirroring `androidMain`'s (SQLDelight's `NativeSqliteDriver`, Ktor's `Darwin` engine, the
   `RecipeRepository` language tag read from `NSLocale` instead of `AppCompatDelegate`), and
   `core:ui`/`core:designsystem` are now Compose Multiplatform modules on `levelchef.kmp.designsystem`
   (the base `levelchef.kmp.feature` builds on): their `strings.xml`/font moved into
   `commonMain/composeResources`, every `stringResource(R.string.x)` call site became
   `stringResource(Res.string.x)`, and `LevelChefTheme`'s typography moved behind a
   `LocalLevelChefTextStyles` composition local (same pattern `LocalLevelChefColors` already used)
   since Compose Multiplatform's resource-backed `Font` loader is `@Composable`, unlike Android's old
   resource-id overload — existing `LevelChefTextStyles.x` call sites are unaffected (property-access
   syntax doesn't change for a `@Composable get()`). `@LevelChefPreview` is now `expect`/`actual`
   (real `@Preview` on Android, a no-op marker on iOS — Compose Multiplatform has no iOS preview
   tooling equivalent yet). The Inter variable font's per-weight axis (`FontVariation`) isn't fully
   supported on iOS yet upstream (JetBrains/compose-multiplatform#3127) — verify visually once an iOS
   build exists. **`feature:*` migration to `levelchef.kmp.feature` — done (Stage D).**
   All seven `feature:*` modules are migrated: `src/main/kotlin` →
   `src/commonMain/kotlin`, `src/main/res/values{,-hu}/strings.xml` →
   `src/commonMain/composeResources/values{,-hu}/strings.xml` (`R.string.x` → `Res.string.x`, with
   both `Res` and each specific resource name imported from `<module>.generated.resources` —
   Compose Multiplatform generates resource accessors as top-level extension properties, not real
   members of `Res`), and each module's Robolectric/Roborazzi screenshot test moved
   `src/test/kotlin` → `src/androidUnitTest/kotlin` (Android-only test deps go in a
   `getByName("androidUnitTest").dependencies { ... }` block, mirroring `data/build.gradle.kts`) —
   including the screenshot baselines (`src/test/screenshots` → `src/androidUnitTest/screenshots`,
   updated in both the `roborazzi { outputDir.set(...) }` build-file setting and the literal path
   string each `captureRoboImage(...)` call passes. Fixes that surfaced migrating these:
   `implementation(platform(libs.compose.bom))` inside a KMP `sourceSets { }.dependencies { }`
   block needs qualifying as `implementation(project.dependencies.platform(...))` (a Kotlin Gradle
   Plugin bug with version-catalog `Provider` notations there — KT-58759's `platform()` deprecation
   is the same code path); an explicit `androidx.lifecycle:lifecycle-viewmodel-compose`
   dependency isn't actually needed (`ViewModel`/`viewModelScope`/`koinViewModel()` already resolve
   transitively through Koin's multiplatform lifecycle artifacts) — it's also the one dependency
   that broke iOS metadata resolution, since the pinned Google coordinate doesn't publish a
   matching iOS variant at the resolved version (by contrast, `feature:home`'s genuine use of
   `androidx.lifecycle:lifecycle-runtime-compose` for `LifecycleEventEffect` resolved fine at the
   same pinned version — the broken resolution was specific to `-viewmodel-compose`, not lifecycle
   Compose artifacts in general); and a helper that returns a raw Android resource id
   (`@StringRes fun x(): Int?`, stored for a later separate `stringResource(it)` call rather than
   called inline) needs its return type changed to `StringResource?` with the Android-only
   `@StringRes` annotation dropped (`feature:home`'s `HomeLabels.kt`; the same pattern recurred as
   an explicitly `Int`-typed function *parameter* in `feature:ingredients`'
   `IngredientFormScreen.kt`'s private `NumberField(labelRes: Int, ...)`, fixed the same way by
   changing the parameter type to `StringResource`; and again as a `Pair<Int, Int>?`-returning
   function in `feature:trophyroom`'s `TrophyRoomLabels.kt` — a badge's title/description resource
   ids held together, fixed by changing the return type to `Pair<StringResource, StringResource>?`).
   `feature:recipedetail` also surfaced an unrelated multiplatform issue worth knowing about for the
   rest: `"%d:%02d".format(...)` (the JVM-only `kotlin.text` formatting extension) doesn't compile
   once a file becomes `commonMain` — watch for any `.format(...)` call in a screen being migrated
   and replace it with a manual string-building equivalent (e.g. `padStart`).
   **`feature:settings`, migrated last, wasn't a drop-in repeat of this recipe** — it genuinely
   depends on Android-only APIs, so it needed real platform code, not just a mechanical move:
   `AppSettingsController` (interface) and `ThemeMode`/`AppLanguage` (enums) went straight to
   `commonMain`; `AndroidAppSettingsController` (the existing `SharedPreferences`/
   `AppCompatDelegate` implementation) moved into `androidMain` unchanged; a new
   `IosAppSettingsController` in `iosMain` now persists for real — the theme under its own
   `NSUserDefaults` key, and the language via the `AppleLanguages` `NSUserDefaults` key (Apple's
   documented in-app language override mechanism, see QA1828). `language()` reads that key back
   directly rather than `NSLocale.preferredLanguages`, since the OS only folds an `AppleLanguages`
   change into `preferredLanguages` on the next launch — reading our own key keeps the in-process
   reflection immediate, the same way `AppCompatDelegate`'s locale is readable right after
   `setApplicationLocales` on Android. `applyPersistedThemeMode()` stays a no-op on iOS: unlike
   `AppCompatDelegate.setDefaultNightMode`, there's no app-wide call to make without a live
   `UIWindow` — that's `iosApp`'s job once it exists (e.g. a SwiftUI root view reading `themeMode()`
   into `.preferredColorScheme`). `data`'s iosMain `databaseModule` still reads
   `NSLocale.preferredLanguages` directly rather than this override (it can't depend on
   `feature:settings`, mirroring the same Android-side constraint), so a language switch here only
   affects recipe generation after the app is relaunched — same caveat the Android seam's own doc
   comment already calls out. `di/SettingsModule.kt` splits the same way `data`'s `databaseModule` does: a
   `commonMain` `settingsCommonModule` (the platform-independent use-case/`ViewModel` bindings)
   that each platform's own top-level `settingsModule` (`androidMain`, `iosMain`) `includes()`,
   adding its own `AppSettingsController` binding. `SettingsRoute.kt`'s three `Context`-touching
   helpers (app version, "rate the app" store link, feedback email) became `@Composable expect`
   functions returning plain callbacks/values — `LocalContext.current` capture happens inside the
   `androidMain` `actual`, where it's still legal to call, mirroring `@LevelChefPreview`'s existing
   `expect`/`actual` shape; the iOS `actual`s (app version, store link, feedback email) stay no-op
   stubs — there's no store listing or email composer to open without an actual `iosApp` yet.
   **A minimal `iosApp` Xcode shell now exists** — a new `:shared` KMP module
   (`shared/build.gradle.kts`, applies `levelchef.kmp.feature` like every `feature:*` module, plus
   the repo's first `binaries.framework { baseName = "LevelChefShared"; isStatic = true }` export
   block) and a hand-built `iosApp/iosApp.xcodeproj` (no CocoaPods — a Run Script build phase calls
   `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, the standard direct-integration
   approach). `shared/src/iosMain/kotlin/com/levelchef/shared/IosEntryPoint.kt`'s `doInitKoin()`
   mirrors `LevelChefApplication.onCreate()`'s `startKoin` call (iOS `databaseModule` + `dataModule`
   + `homeModule`, blank `geminiApiKey`), and `MainViewController()` boots
   `LevelChefTheme { HomeRoute() }` via `ComposeUIViewController` — real Koin-wired data (SQLDelight
   `NativeSqliteDriver`, Ktor `Darwin`), no navigation.

   **Material3-on-iOS crash — found and fixed.** Manually running the shell in the simulator
   (`xcrun simctl launch --console` to see stdout) originally threw `Error was captured in
   composition.` from inside Compose's own composition error boundary, with no further detail.
   Bisecting proved it wasn't `HomeRoute`, not `LevelChefTheme`, not the Inter variable font — a
   **bare** `MaterialTheme { Text("...") }` with zero app code crashed identically. Root cause: CMP
   `1.7.3`'s `material3` iOS library was compiled against `kotlinx-datetime 0.6.0`, while this repo
   pinned `0.8.0` — a klib binary mismatch (`PlatformDateFormat.darwin.kt` linking with `Can not get
   instance of singleton 'Companion': No class found for symbol
   'kotlinx.datetime/Instant.Companion|null[0]'`) that Kotlin/Native's partial-linkage feature turned
   into a runtime-throwing stub `MaterialTheme`'s init touched immediately. Fixed by bumping the
   whole toolchain forward rather than downgrading `kotlinx-datetime`: **Kotlin `2.4.10`** (pinned to
   exactly this, not just "2.1.0+", to match `detekt 2.0.0-alpha.6`'s own exact Kotlin requirement —
   each `detekt 2.0.0-alpha.N` pins one specific Kotlin version) and **Compose Multiplatform
   `1.11.0`**, whose `material3` depends on `kotlinx-datetime 0.7.1+` — compatible with this repo's
   `0.8.0`, no downgrade needed (material3 versioning decoupled from CMP's own starting CMP 1.9;
   confirmed by resolving `:feature:settings`'s `iosArm64CompileKlibraries` configuration and reading
   `material3:1.9.0 -> kotlinx-datetime:0.7.1 -> 0.8.0`, no mismatch). **Pinned to exactly `1.11.0`,
   not CMP's newer releases**: `1.12.1`'s Android-interop artifacts (`androidx.compose.*` `1.12.1`)
   require `compileSdk 37` + AGP `9.1.0`+, and AGP 9's built-in Kotlin support in turn rejects
   `com.android.library` + `org.jetbrains.kotlin.multiplatform` together — every KMP module in this
   repo. AGP's own error names the fix as migrating to `com.android.kotlin.multiplatform.library`
   (a different plugin, different DSL — single-variant only, `androidResources`/tests opt-in, source
   sets possibly renamed) — a real, separate migration project, not a version bump, and its own
   suggested escape hatch (`android.builtInKotlin=false`/`android.newDsl=false` in `gradle.properties`)
   doesn't reach precompiled script convention plugins like this repo's at all (tried it in both the
   root and `build-logic`'s own `gradle.properties`, and as explicit `-P` flags — identical failure
   every time; Gradle's type-safe-accessor generation for precompiled script plugins evaluates each
   plugin in an isolated context that doesn't read Gradle properties). `1.11.0` needs none of this —
   compileSdk 36 / AGP 8.13.2 unchanged. Revisit once/if the `com.android.kotlin.multiplatform.library`
   migration is worth doing on its own. Two other things broke and got fixed along the way: `iosX64()`
   (Intel simulator) had to be dropped from every KMP convention plugin and `shared/build.gradle.kts`
   — CMP stopped publishing artifacts for it somewhere past `1.7.3` (`iosArm64`/`iosSimulatorArm64`
   are what any real Apple Silicon dev machine or this repo's CI actually use, so nothing is lost);
   and `compose.material3` stopped transitively pulling in `material-icons-core` on iOS the way it
   did in `1.7.3`, so `levelchef.kmp.designsystem` now explicitly adds
   `implementation(compose.materialIconsExtended)` (the only icons accessor CMP's
   `ComposePlugin.Dependencies` exposes — there's no separate "core-only" one). **That same
   material-icons-core breakage also hit the Android target** for the two modules that aren't
   `levelchef.kmp.*`-based and so never got that fix transitively (`implementation`-scoped, invisible
   to project-dependency consumers): `androidApp` and `feature:onboarding` (see below — not yet
   migrated to KMP). Both now explicitly add the plain Jetpack Compose equivalent (new
   version-catalog entry `compose-material-icons-extended`,
   `androidx.compose.material:material-icons-extended`) in
   `levelchef.android.application`/`levelchef.android.feature`.
   This was caught building the Android target locally with a real Android SDK — the dev machine that
   found and fixed the original iOS crash had none configured, so it couldn't compile-check
   `androidApp` or `feature:onboarding` at all. **Still needs re-verification on an actual iOS
   Simulator**: everything above was chosen and compile-verified from a Windows machine (Kotlin/Native
   klib compilation for `iosArm64`/`iosSimulatorArm64` works fine there; only the final framework
   *link* and anything simulator-side needs actual Xcode/macOS) — the original Material3 crash fix was
   confirmed on-device at `1.12.1`, not at this `1.11.0` pin, so re-confirm the crash is still gone
   before trusting this.

   Deliberately **not** done yet, so don't assume they work: `feature:onboarding` isn't KMP (`OnboardingGate` is skipped entirely on iOS —
   the shell shows Home directly), the full bottom-nav graph isn't in `:shared` (`androidx.navigation:navigation-compose`
   hasn't been proven on iOS in this repo), there's no real Gemini key path for iOS (recipe
   recommendations always use the bundled fallback), and `applyPersistedThemeMode`'s iOS
   `.preferredColorScheme` wiring (noted above) still isn't connected to a live `UIWindow`.
2. **DB migration policy flips at the first release.** The `core:database` section above
   documents the pre-release exception: no `migrations/N.sqm` files yet, `.sq` files are edited
   directly and dev devices just clear app data. That exception ends the moment a build is
   actually released — the very next PR that changes the SQLDelight schema after that point must
   ship a `migrations/N.sqm` file (version = migration count + 1, no data in migrations) plus a
   `data` `androidUnitTest` upgrading a hand-built old database, and must extend `SM-14` in
   `docs/QA_REGRESSION.md` with an upgrade check for the new data. Update this note (and
   `core:database`'s wording above) to drop the pre-release exception once that first release ships.
