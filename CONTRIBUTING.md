# Contributing to LevelChef

Solo project, but run like a team project on purpose. `AGENTS.md` covers architecture and
code conventions; this file covers the Git / CI workflow.

## One-time setup

```bash
git config core.hooksPath .githooks     # enables the Conventional Commits check
git config commit.template .gitmessage  # pre-fills the commit message format
```

Requires the Android SDK locally (`local.properties` → `sdk.dir`), JDK 17+ (JDK 21 is fine —
the Gradle toolchain resolver fetches JDK 17 for `build-logic` automatically).

## Branching

Trunk-based. `main` is protected: no direct pushes, PR + green CI required, linear history.

- Branch off `main`, keep it short-lived: `feat/…`, `fix/…`, `chore/…`, `docs/…`, `ci/…`,
  `refactor/…` (e.g. `feat/recipedetail-screen`).
- One PR = one concern.
- **Squash-merge only.** The PR title becomes the commit subject, so it must be a valid
  Conventional Commit (CI enforces this). Delete the branch after merge.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<optional scope>): <subject>

type:  feat | fix | chore | docs | refactor | test | build | ci | perf | style | revert
scope: a module, lowercase — (home), (core:designsystem), (data), (domain) …
```

Examples: `feat(home): add weekly challenge card`, `chore: bump detekt to 2.0.0-alpha.7`.
The `.githooks/commit-msg` hook rejects non-conforming subjects locally.

## Before pushing

```bash
./gradlew build detekt :konsist:test
```

- `detekt` — static analysis + ktlint formatting + Compose lint rules
  (config: `config/detekt/detekt.yml`). `./gradlew detekt --auto-correct` fixes formatting.
- `:konsist:test` — architecture tests that enforce the `AGENTS.md` module rules.
- `build` — compiles every module and runs Android lint + unit tests.

## CI

`.github/workflows/ci.yml` runs the command above on every PR and push to `main`, plus
Gradle wrapper validation. `.github/workflows/pr-title.yml` validates the PR title.
Reports (detekt, lint, tests) are uploaded as build artifacts.
