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

Also run `./gradlew koverVerify` when you touch logic (use cases, repository impls, ViewModels,
domain mappers) — the 90% line-coverage gate. Screenshot baselines: `./gradlew
:feature:<name>:recordRoborazziDebug` and commit the PNGs when a screen changes visually.

## Definition of done for a user-facing change

A PR that adds or changes anything a user can see or do — a screen, a flow, a setting, a
navigation path, persisted data, user-facing copy — also **updates
[`docs/QA_REGRESSION.md`](docs/QA_REGRESSION.md) in the same PR**: add or edit the relevant
`SM-NN` scenario, add its results-log row, and bump the file's "Last updated" line. See that
file's *Extending this script* checklist. Reviewers reject PRs that skip this.

## CI

`.github/workflows/ci.yml` runs the command above on every PR and push to `main`, plus
Gradle wrapper validation. `.github/workflows/pr-title.yml` validates the PR title.
Reports (detekt, lint, tests) are uploaded as build artifacts.
