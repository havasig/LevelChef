# Tooling & automation reference

An inventory of every tool, check, and automation wired into this repo — build system,
static analysis, CI, Git and GitHub automation, and the AI-agent setup. **This is a list.**
Each entry is one line now; explanations can be filled in per entry later.

For *architecture* and code conventions see [`AGENTS.md`](../AGENTS.md); for the
*day-to-day Git workflow* see [`CONTRIBUTING.md`](../CONTRIBUTING.md).

---

## 1. Build system

- **Gradle wrapper** — `gradlew` / `gradlew.bat`, pinned to Gradle **9.0.0** (`gradle/wrapper/gradle-wrapper.properties`).
- **Version catalog** — `gradle/libs.versions.toml`: every dependency version and coordinate lives here, referenced as `libs.*`. No hardcoded coordinates in module build files.
- **Convention plugins** — `build-logic/convention/src/main/kotlin/`, applied instead of repeating config:
  - `levelchef.android.application` — the `androidApp` module (compileSdk/minSdk, Compose, JVM target).
  - `levelchef.android.library` — plain Android library modules (`core:ui`, `core:designsystem`).
  - `levelchef.android.feature` — feature modules; layers `core:ui` + `core:designsystem` + Compose tooling on top of `android.library`.
  - `levelchef.kmp.library` — Kotlin Multiplatform modules (`core:model`, `core:database`, `domain`, `data`).
- **foojay toolchain resolver** — `settings.gradle.kts` + `build-logic/settings.gradle.kts`; lets Gradle auto-download the JDK a build needs (JDK 17 for `build-logic`) so any JDK 17+ can run the build.
- **`.editorconfig`** — repo-root; drives IntelliJ + ktlint formatting (4-space indent, 120 col, LF, final newline, trailing commas allowed; CRLF for `*.bat`).
- **`gradle.properties`** — `kotlin.code.style=official`, `android.useAndroidX`, `android.nonTransitiveRClass`, KMP android source-set layout v2; **`-Xmx4g`** heap; **`org.gradle.parallel`**, **`org.gradle.caching`** (build cache — task outputs reused across builds) and **`org.gradle.configuration-cache`** (config phase skipped when nothing structural changed) all on. CI persists both caches across runs via `gradle/actions/setup-gradle` (the config cache is encrypted with the `GRADLE_ENCRYPTION_KEY` repo secret).

## 2. Static analysis & formatting

- **detekt** — Kotlin static analysis; single root aggregate task `./gradlew detekt` scanning every module's `.kt`/`.kts` (applied only to the root project — detekt `2.0.0-alpha.6`'s plugin is incompatible with Kotlin Gradle Plugin 2.0.x when combined with the Android plugin).
- **`config/detekt/detekt.yml`** — the rule config: deviations from detekt defaults only (Compose-friendly complexity thresholds, 120-col lines, PascalCase composables & design tokens).
- **detekt-rules-ktlint-wrapper** — ktlint formatting rules run inside detekt; `./gradlew detekt --auto-correct` fixes formatting in place.
- **compose-rules** (`io.nlopez.compose.rules:detekt` `0.6.6`) — Compose-specific lint: `Modifier` defaults & naming, param ordering, state hoisting, preview naming, unstable collections, etc.
- **Android Lint** — bundled with AGP; runs per Android module as part of `./gradlew build` (or `./gradlew lint`). HTML reports under `<module>/build/reports/`.
- **Kover** (`org.jetbrains.kotlinx.kover` `0.9.9`) — code coverage, aggregated at the repo root. `./gradlew koverVerify` **fails if line coverage of the logic layer drops below 90%**; `./gradlew koverHtmlReport` → `build/reports/kover/html/index.html`. Scope = `com.levelchef.domain.usecase.*`, `com.levelchef.data.repository.*`, feature `*ViewModel` + `*DomainMappersKt`; all `@Composable` code is excluded (Kover 0.9 can't filter individual verify rules, so the reports carry the same scope). The plugin is applied per-module — add `alias(libs.plugins.kover)` + a root `kover(project(...))` entry when a feature gets a ViewModel. Currently **100%**.
- Current status: **0 detekt findings**, lint clean, coverage 100% of the logic layer.

## 3. Architecture enforcement — Konsist

- **`:konsist` module** — a test-only JVM module (`konsist/`) that parses the whole source tree and fails the build on architecture-rule violations. Runs via `./gradlew :konsist:test` (and as part of `build`). **14 tests.**
- **`ModuleBoundaryKonsistTest`** — the one-way dependency rules:
  - no `feature:*` module imports another `feature:*` module;
  - `domain` and `core:model` import no Android / Compose / Koin / Ktor / SQLDelight;
  - `domain` imports only `core:model` (within the project);
  - `core:database` is imported by `data` only.
- **`ComposeScreenKonsistTest`** — the screen pattern from `AGENTS.md`:
  - every top-level `*Screen` / `*Route` function is `@Composable`;
  - every `*UiState` is a `data class` with **every** constructor parameter defaulted, living in a `feature:*` package.
- **`ConventionsKonsistTest`** — naming & packaging:
  - all source is under `com.levelchef`;
  - `@Test` function names are `snake_case`, no backticks;
  - `*ViewModel` classes live in a `feature:*` package;
  - `*Repository` interfaces live in `domain`, `*RepositoryImpl` in `data`;
  - `core:model` files declare exactly one public top-level type.

## 4. Testing

- **`kotlin.test` + `commonTest`** — KMP module unit tests (`kotlin.test.Test`, `assertEquals`); coroutines via `runTest` (`kotlinx-coroutines-test`).
- **Turbine** (`app.cash.turbine` `1.2.1`) — `Flow` / `StateFlow` assertions: `flow.test { awaitItem(); … }`. Used in `CookingSessionRepositoryImplTest` (SQLDelight `observeAll()`) and `HomeViewModelTest` (`uiState`).
- **Hand-written fakes** — `private class Fake…Repository` implementing the domain interface (see `domain/src/commonTest/.../GetChefLevelUseCaseTest.kt`).
- **In-memory SQLDelight** — `CookingSessionRepositoryImplTest` runs the real schema via `JdbcSqliteDriver(IN_MEMORY)` (`app.cash.sqldelight:sqlite-driver`, in `data`'s `androidUnitTest`).
- **Roborazzi + Robolectric** (`1.73.0` / `4.16.1`) — screenshot tests, JVM, no emulator. `<Name>ScreenScreenshotTest` renders `LevelChefTheme { <Name>Screen(...) }` and calls `captureRoboImage()`. Baselines committed under `feature/<name>/src/test/screenshots/`. `./gradlew :feature:home:recordRoborazziDebug` regenerates them; `verifyRoborazziDebug` checks them; `compareRoborazziDebug` produces the `*_compare.png` strips. Applied per-module (`feature/home/build.gradle.kts`). Pilot: `feature:home`; each screen adds its own test as it is built.
- **JUnit 5** — used only by the `:konsist` module (`org.junit.jupiter`, `junit-platform-launcher`).
- **Koin `module.verify()`** — `androidApp/src/test/.../KoinModulesVerifyTest.kt` combines `databaseModule + dataModule + homeModule` via `module { includes(...) }` (cross-module dependencies aren't visible to per-module verification) and calls `.verify()` — a static check that every constructor dependency in the real `startKoin` graph has a matching binding, without booting the app.
- **Coverage gate** — see Kover under §2; `./gradlew koverVerify` (90% logic-layer line coverage).
- Commands: `./gradlew :domain:allTests` (KMP), `./gradlew :domain:testDebugUnitTest` (Android variant), `./gradlew :konsist:test`, `./gradlew koverVerify`.

## 5. CI pipeline — `.github/workflows/ci.yml`

- **Triggers** — every push to `main` and every PR targeting `main`; in-progress runs for the same ref are cancelled.
- **Runner** — `ubuntu-latest`, 30-min timeout, permissions `contents: read` + `checks: write` + `pull-requests: write`.
- **Steps, in order:**
  1. `actions/checkout@v4`.
  2. `actions/setup-java@v4` — Temurin JDK 21 (foojay fetches JDK 17 for `build-logic`).
  3. `gradle/actions/setup-gradle@v4` with `validate-wrappers: true` + `cache-encryption-key` — restores the Gradle build **and configuration** cache, and verifies `gradle-wrapper.jar` checksums (supply-chain check).
  4. `./gradlew build detekt :konsist:test koverXmlReport koverHtmlReport --stacktrace` — compile every module + Android lint + unit tests + detekt + architecture tests + coverage reports.
  5. `mikepenz/action-junit-report@v5` — turns `**/build/test-results/**/TEST-*.xml` into PR check annotations.
  6. `madrapps/jacoco-report@v1.7.1` (PRs only) — posts/updates a **Logic-layer coverage** comment from `build/reports/kover/report.xml`.
  7. `./gradlew koverVerify` — **the coverage gate**; fails the `build` check (and blocks the PR) if logic-layer line coverage < 90%.
  8. `actions/upload-artifact@v4` — uploads `build-reports` (all `build/reports/**` + `build/test-results/**`), kept 7 days.
- **Cold run ≈ 6–11 min**; warm runs reuse the Gradle build + configuration cache and are much faster.

## 6a. Screenshot comparison — `.github/workflows/screenshots.yml`

- Runs on PRs that touch `feature/**`, `core/ui/**`, `core/designsystem/**` or the version catalog.
- `./gradlew :feature:home:compareRoborazziDebug` renders each screen and diffs it against the committed baseline — **never fails**.
- If any screen changed: the `*_compare.png` strips (baseline | this PR | diff) are pushed to the `ci/screenshots` orphan branch and posted as an **inline PR comment** (updated in place on each push).
- To accept a change: run `./gradlew :feature:home:recordRoborazziDebug` locally and commit the new PNGs — the baseline update then rides through normal PR review.
- Needs `contents: write` (only ever writes to `ci/screenshots`) + `pull-requests: write`. Skipped for fork PRs (read-only token).

## 6c. Release notes — `.github/workflows/release-drafter.yml`

- On every push to `main`: drafts/updates a GitHub Release, categorizing merged (squash-merge) PR titles by Conventional Commit type — `feat` → Features, `fix` → Fixes, `perf`/`refactor` together, `test`, `docs`, `chore`/`ci`/`build`/`style` together (`.github/release-drafter.yml`).
- Version bump: minor on any `feat`, patch otherwise (`major` needs an explicit `breaking` label).
- Nothing is published automatically — the draft sits under Releases until someone hits publish.

## 6e. Automated PR review — `Dangerfile.js`

- `danger/danger-js` via `npx danger ci` on every PR (the only workflow with a Node toolchain — everything else stays Kotlin-only). Every check is `warn`, never a failure.
- Warns on: a PR touching more than 40 files; `feature/*/src/main/**` changed with no matching `feature/*/src/test/screenshots/**` update; a new `*ViewModel.kt` with no matching `*ViewModelTest.kt`.

## 6. PR-title check — `.github/workflows/pr-title.yml`

- **`amannn/action-semantic-pull-request@v5`** — validates the PR **title** is a Conventional Commit; runs on PR open/edit/synchronize/reopen.
- Allowed types: `feat fix chore docs refactor test build ci perf style revert`. Scope optional. Subject must start lowercase and not end with a period.
- Why: PRs are **squash-merged**, so the PR title becomes the commit subject on `main`.
- Note: only runs on PRs once this workflow file is on `main` (it wasn't present for PR #1, which introduced it).

## 7. Branch protection — GitHub ruleset `main` (id `22110858`)

- **Pull request required** — no direct pushes to `main` (0 approvals required, solo repo).
- **Status check `build` required**, strict — the branch must be up to date and CI green to merge.
- **Linear history** — no merge commits.
- **Squash merge only** — merge & rebase buttons disabled.
- **Force-push and branch deletion blocked.**
- Verified: a direct `git push origin main` is rejected by the server.
- Change it: `gh api -X PUT/DELETE repos/havasig/LevelChef/rulesets/22110858` or GitHub → Settings → Rules → Rulesets.
- (Requires the repo to be **public** or on GitHub Pro — it is currently public.)

## 8. Git conventions & automation

- **Conventional Commits** — `type(scope): subject`. Examples: `feat(home): add weekly challenge card`, `fix(data): handle empty recipe list`, `chore: bump detekt`.
- **`.githooks/commit-msg`** — POSIX-sh hook that rejects non-conforming commit subjects locally (no Node/tooling needed). Allows `Merge`/`Revert`/`fixup!` messages.
- **`.gitmessage`** — commit-message template (the format reminder appears in your editor).
- **One-time activation per clone:** `git config core.hooksPath .githooks && git config commit.template .gitmessage` (documented in `CONTRIBUTING.md`; already active in this clone).
- **Branch naming** — `feat/…`, `fix/…`, `chore/…`, `docs/…`, `ci/…`, `refactor/…`; short-lived, one concern each.
- **`.gitattributes`** — normalizes line endings (LF in repo, CRLF for `*.bat`/`*.cmd`, binary assets untouched).
- **`.gitignore`** — build outputs, IDE files, `local.properties`, `.claude/settings.local.json`.

## 9. GitHub templates

- **`.github/pull_request_template.md`** — Summary · Modules touched · Screenshots (required for `feature:*` UI changes) · checklist (build+detekt+konsist pass, no feature→feature dep, domain/model purity, English-only text, nav wired, tests).
- **`.github/ISSUE_TEMPLATE/feature_screen.yml`** — form for building a stub screen; dropdown pre-filled with the 5 remaining screens and their Figma node IDs.
- **`.github/ISSUE_TEMPLATE/bug_report.yml`**, **`chore.yml`** — structured forms; titles pre-seeded with `fix:` / `chore:`.
- **`.github/ISSUE_TEMPLATE/config.yml`** — disables blank issues; adds a link to the Figma file.
- **`.github/CODEOWNERS`** — `* @havasig` (auto-requests review).

## 10. AI agent setup

- **`AGENTS.md`** — the single source of truth for any AI coding agent: module rules, the Compose screen pattern, conventions, build commands, Git workflow.
- **`CLAUDE.md`** — imports `AGENTS.md` (`@AGENTS.md`); holds Claude-Code-specific notes only.
- **`.github/copilot-instructions.md`** — one-line pointer to `AGENTS.md` so GitHub Copilot reads the same rules.
- **`.claude/settings.json`** — committed Claude Code config; a permission allowlist for `./gradlew` and safe `git` subcommands so routine commands don't prompt, plus a `Stop` hook that best-effort runs `./gradlew detekt --auto-correct -q` at the end of every session (formatting only, non-blocking — it reports what it found but never prevents the session from stopping).
- **`.claude/settings.local.json`** — personal Claude Code overrides, gitignored.
- **`.claude/skills/new-feature-screen/SKILL.md`** — a reusable workflow ("skill") for turning a Figma node into a full Compose feature screen following the `feature:home` pattern.
- **Figma MCP** (user-level, not in the repo) — lets the agent pull designs, screenshots and design tokens from the LevelChef Figma file during screen work.
- **Claude Code memory** (user-level) — persists facts about the project across sessions (e.g. "portfolio project, optimize for interview quality", "English-only codebase").

## 11. Command cheat-sheet

```bash
./gradlew build                      # compile everything + Android lint + unit tests
./gradlew detekt                     # static analysis + ktlint + Compose rules (root task)
./gradlew detekt --auto-correct      # auto-fix formatting
./gradlew :konsist:test              # architecture rule tests
./gradlew :domain:allTests           # KMP module tests
./gradlew lint                       # Android lint only
./gradlew :androidApp:installDebug   # deploy to a connected device/emulator

./gradlew build detekt :konsist:test # run before every push (same as CI)

# one-time, per clone:
git config core.hooksPath .githooks
git config commit.template .gitmessage
```

## 12. Not set up yet (roadmap)

Each is a tracked issue — [`tooling` label](https://github.com/havasig/LevelChef/labels/tooling).

- **[#5](https://github.com/havasig/LevelChef/issues/5) dependency-analysis plugin** — flags unused / misdeclared dependencies and `api` vs `implementation` mistakes.
- **[#6](https://github.com/havasig/LevelChef/issues/6) Renovate** — automated dependency-update PRs, grouped for the version catalog.
- **[#7](https://github.com/havasig/LevelChef/issues/7) CodeQL** — GitHub-native security scanning for Kotlin/Java.

**Done:** ~~#2 Gradle build + configuration cache~~ (§1) · ~~#3 Kover~~ (90% logic gate, §2) · ~~#4 Roborazzi screenshot tests~~ (§4, §6a) · ~~#8 release-drafter~~ (§6c) · ~~#9 Danger~~ (§6e) · ~~#10 LICENSE~~ · ~~#11 Claude Code hooks~~ (§10) · ~~#12 Koin `module.verify()`~~ (§4) · ~~#13 Turbine~~ (§4).
