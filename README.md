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
- **`core:database`** — KMP: the SQLDelight schema (cooking sessions, survey response, pantry, saved recipes, badges, weekly challenges). Still at a v1 baseline with no migrations — see AGENTS.md.
- **`domain`** — KMP: repository interfaces (`CookingSessionRepository`, `RecipeRepository`, `UserProfileRepository`) and use cases, depends only on `core:model`. Kept intentionally thin — `GetChefLevelUseCase` is the only use case, since it's the only one with real logic beyond a 1:1 repository delegation; has its own unit tests (`domain/src/commonTest`).
- **`data`** — KMP: repository implementations backed by `core:database` (SQLDelight) and a Koin DI wiring (`dataModule` / `databaseModule`); `RecipeRepositoryImpl` generates recommendations from the stored survey and the current app language via the Gemini API (Ktor), caching each batch in SQLDelight keyed by both, and falling back to a small bundled recipe set (English or Hungarian) when there's no API key, no survey yet, or Gemini/the cache are unavailable. Reaches Gemini through the `proxy/` Cloudflare Worker (the app never holds the API key) — set `RECIPE_PROXY_URL` in `local.properties` to enable it, see "Recipe proxy" below.
- **`core:ui`** — Compose theme (`Color`/`Theme`/`Type`). Follows the system light/dark setting; both palettes come from the Figma file (dark: `#0f0f1a` background, `#534AB7` accent). Flat/no-shadow cards, 12px radius, 0.5px borders.
- **`core:designsystem`** — reusable Compose components (`LevelChefBadge`, `LevelChefTag`, `LevelChefDivider`, ...), depends on `core:ui`.
- **`feature:home`** — fully implemented from Figma node `296:1929`: level pill + XP bar, stat cards, weekly challenge card, primary CTA, recipe recommendations, last-cooked card. Wired end-to-end: `HomeRoute` (stateful) resolves a Koin-injected `HomeViewModel`, which loads real data from `domain` (profile, chef level, weekly challenge, last-cooked session, recipe recommendations) into the stateless `HomeScreen`/`HomeUiState`.
- **`feature:onboarding`, `feature:settings`, `feature:ingredients`, `feature:recipedetail`, `feature:mealreview`, `feature:trophyroom`, `feature:cookinglog`** — one module per screen, all fully built out from their Figma nodes. `feature:cookinglog` is the real content of the bottom-nav **Recipes** tab (search + All/Cooked/New saved-recipes list), not a separate drill-down.
- **`androidApp`** — the application shell: `MainActivity`, bottom-nav `NavHost` wiring the Home / Recipes / Trophies tabs, Koin startup (`LevelChefApplication`, registers every module's DI module). Depends on every module above but nothing depends on it.

Dependency direction is one-way: `feature:*` → `core:designsystem`/`core:ui` (+ `domain`/`data` once a feature needs real data), `data` → `domain` → `core:model`, `core:database` is only referenced from `data`. No feature module depends on another.

## Next steps

1. Open in Android Studio, let Gradle sync (needs the Android SDK installed locally).
2. Deploy the recipe proxy (see "Recipe proxy" below) and add `RECIPE_PROXY_URL=<worker url>` to
   `local.properties` (gitignored) to enable live recipe recommendations — without it,
   `RecipeRepositoryImpl` serves its bundled fallback recipes instead.
3. See [`AGENTS.md`](AGENTS.md)'s "Not yet done" section for the current list of open work
   (the iOS app shell and re-recording screenshot baselines) — kept there rather than duplicated
   here so it doesn't drift out of sync.

## Recipe proxy (Gemini access + usage limits)

Live recommendations go through a small Cloudflare Worker in [`proxy/`](proxy/), so the Gemini API
key stays server-side (a key inside the APK can be extracted by anyone) and usage is capped where a
modified client cannot bypass it. **The app never sends a prompt**: it posts only the onboarding
answers (enum names) and the content language (`{"survey": {…}, "language": "en" | "hu"}`); the Worker
validates every value against a whitelist and builds the prompt and response schema itself, so the
endpoint can't be used as a free general-purpose Gemini relay. Per UTC day, only *successful*
generations count against the first three limits (the quota is reserved before the Gemini call and
refunded if it fails):

| Limit | Default | Setting (`proxy/wrangler.toml`) |
|---|---|---|
| per device (random per-install id) | 20 while the app has a single user (lower to e.g. 5 before sharing it) | `DEVICE_DAILY_LIMIT` |
| per client IP | 20 | `IP_DAILY_LIMIT` |
| global, all users | 20 | `GLOBAL_DAILY_LIMIT` |
| upstream attempts per IP, failed ones included | 60 | `ATTEMPT_DAILY_LIMIT` |
| burst, per IP | 3 / minute | `[[ratelimits]]` `BURST` binding |

**`GLOBAL_DAILY_LIMIT` must not exceed the Gemini key's own daily quota** — the free tier allows only
20 `generateContent` requests a day for the model, which is why the default is 20. Enable billing on
the Gemini project before raising it. Also note the free tier lets Google use prompts to improve its
products; the prompt contains the user's diet and allergens, so a paid tier is the right choice for
a public release (see [`docs/PRIVACY.md`](docs/PRIVACY.md)).

Over a limit the proxy answers HTTP 429 (with `Retry-After`) and the app quietly serves its cached
recipes or the bundled ones that fit the user's diet and allergens, and doesn't ask again until the
`Retry-After` (or 5 minutes after any other failure) has passed. A Gemini 503 is retried once.
The Worker also pins the model and caps the body (2 KB) and output tokens.

One-time setup (needs a Cloudflare account and a Gemini key from Google AI Studio):

```bash
cd proxy && npm install
npx wrangler kv namespace create RATE_LIMITS   # paste the printed id into wrangler.toml
npx wrangler secret put GEMINI_API_KEY
npx wrangler deploy                            # prints https://levelchef-recipe-proxy.<you>.workers.dev
```

Then set `RECIPE_PROXY_URL=<that url>` in `local.properties` (dev builds) and as the repository
**variable** `RECIPE_PROXY_URL` (release builds; not a secret). Also set a monthly budget alert and
quota cap on the Gemini API project in Google Cloud as the last line of defence. `npm test` in
`proxy/` runs the Worker's unit tests. KV is eventually consistent and not atomic; reserving the quota
before the upstream call plus the per-IP burst limiter keep concurrent requests from overshooting
meaningfully. **The app and the Worker share the request contract — deploy the Worker together with
an app release that changes it.**

Releasing on Google Play — tracks, policy forms, store texts: [`docs/PLAY_RELEASE.md`](docs/PLAY_RELEASE.md),
[`docs/STORE_LISTING.md`](docs/STORE_LISTING.md), [`docs/PRIVACY.md`](docs/PRIVACY.md), [`docs/TERMS.md`](docs/TERMS.md).

## Signing a release build

`androidApp`'s `release` build type is unsigned by default, falling back to debug signing so
`./gradlew build`/CI keep working without a keystore. To produce a real, Play Store–ready build,
generate a keystore (`keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048
-validity 10000 -alias levelchef`), keep the `.jks` file **out of the repo** (already gitignored),
and supply these four properties — either in `local.properties` on a release manager's machine, or
as identically named env vars in a CI/CD release job:

```
RELEASE_STORE_FILE=/absolute/or/root-relative/path/to/release.jks
RELEASE_STORE_PASSWORD=<store password>
RELEASE_KEY_ALIAS=<key alias>
RELEASE_KEY_PASSWORD=<key password>
```

With all four set, `./gradlew :androidApp:bundleRelease` / `assembleRelease` produce a
properly-signed artifact. `bundleRelease` (the Play upload) **fails** without them rather than
produce a debug-signed bundle.

Release builds are shrunk, optimized and obfuscated by R8 (`androidApp/proguard-rules.pro`). To
smoke-test exactly that minified build on a device without the release key — and without wiping a
debug install — use the `qa` build type: `./gradlew :androidApp:installQa` installs it as
`com.levelchef.android.qa`, next to the debug app (QA script SM-27).

### Publishing a GitHub release

`.github/workflows/release.yml` builds the signed APK and attaches `LevelChef-vX.Y.Z.apk` (plus a
`.sha256` checksum) to the GitHub release. It needs four repository secrets
(**Settings → Secrets and variables → Actions**):

| Secret | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | the `.jks` file, base64-encoded (`base64 -w0 release.jks`) |
| `RELEASE_STORE_PASSWORD` | store password |
| `RELEASE_KEY_ALIAS` | key alias |
| `RELEASE_KEY_PASSWORD` | key password |

Flow: release-drafter keeps a draft release up to date on every merge to `main` → bump
`versionName`/`versionCode` in `androidApp/build.gradle.kts` so it matches the draft's tag
(`v<versionName>`) → **publish** the draft; the workflow runs for that tag and attaches the APK. To
(re)attach an APK to an existing release (including a still-unpublished draft), run the workflow
manually with that tag. It fails rather than ship a debug-signed APK, and it reads the optional repository
variable `RECIPE_PROXY_URL`; without it, published builds use the bundled recipe set.
