# Releasing on Google Play

A step-by-step guide for the first LevelChef release and later updates. Play Console changes its
menus from time to time; the names below match it as of late 2026. Check anything marked ⚠️ against
the current [Play Console Help](https://support.google.com/googleplay/android-developer) before
relying on it.

## The testing tracks in one minute

Play has four tracks. A build uploaded to one can be promoted to the next without rebuilding.

| Track | Who can install it | Review | What it's for |
|---|---|---|---|
| **Internal testing** | Up to 100 testers you name by e-mail (Google accounts) | None or very short — usually installable within minutes | Checking that the *Play-delivered* build works: real signing, R8, app bundle splitting. Ideal for a single-user / personal app — you can live on this track forever. |
| **Closed testing** | An e-mail list or Google Group you invite | Full review | A wider beta; ⚠️ **required before production for new personal developer accounts** (see below) |
| **Open testing** | Anyone who opts in from the store page | Full review | Public beta |
| **Production** | Everyone | Full review | The real release |

**What internal testing gives you that installing the APK yourself doesn't:** the exact file users
would get (Play re-signs it with the app-signing key and splits the bundle per device), automatic
updates through the Play Store, and a check that the listing, policy forms and signing are all in
order — before anyone else sees the app.

## ⚠️ New personal accounts: the closed-testing requirement

Personal developer accounts created after 13 November 2023 must, before they can publish to
production, run a **closed test with at least 12 testers who stay opted in for 14 consecutive
days**, then apply for production access (Play Console → Dashboard → "Apply for production"). An
internal test does **not** count. If you only use the app yourself, you don't need production at
all: stay on internal testing.

## One-time setup

1. **Developer account** — [play.google.com/console](https://play.google.com/console), one-time
   USD 25 fee, identity verification (ID document; can take a few days). Choose a *personal* account.
2. **Create app** — name "LevelChef", default language English (United States), App, Free.
   Free apps can never be made paid later.
3. **App signing** — keep the default **Play App Signing**: Google holds the app-signing key; your
   `release.jks` becomes the *upload key*. Back `release.jks` and its passwords up somewhere safe
   (password manager) — losing the upload key needs a support request to reset.
4. **Policy → App content** — every form must be complete before any release, including internal:
   - **Privacy policy**: public URL of [`PRIVACY.md`](PRIVACY.md)'s policy text (a GitHub Pages
     page, Google Sites or a public Notion page all work).
   - **App access**: "All functionality is available without special access" (no login).
   - **Ads**: No.
   - **Content rating**: questionnaire → category *Utility, Productivity, Communication, or Other*;
     answer No to violence/sexual content/gambling etc. → expect "Everyone / PEGI 3".
   - **Target audience**: 18+ (or 13+); **not** children — avoids the Families policy requirements.
   - **Data safety**: answers in [`PRIVACY.md`](PRIVACY.md#play-console-data-safety--suggested-answers)
     — they must match the privacy policy.
   - **Health apps declaration** ⚠️ (all apps must fill it): LevelChef is *Nutrition and weight
     management*-adjacent at most; declare that it handles diet/allergy info and is not a medical device.
   - **Government apps / Financial features / News**: No.
   - **AI-generated content** ⚠️: Play's generative-AI policy expects a way for users to **report
     or flag** offensive AI output from inside the app. LevelChef doesn't have one yet — see
     [Open items](#open-items).
5. **Store listing** — texts and graphics from [`STORE_LISTING.md`](STORE_LISTING.md).

## Every release

1. **Bump the version** in `androidApp/build.gradle.kts`: `versionCode` +1 (Play rejects a code it
   has seen), `versionName` e.g. `0.2.0`.
2. **Deploy the proxy** if `proxy/` changed: `cd proxy && npx wrangler deploy` — the app and the
   Worker share the request contract.
3. **Smoke-test the minified build**: `./gradlew :androidApp:installQa` and run QA script SM-27
   (plus the full [`QA_REGRESSION.md`](QA_REGRESSION.md) for a bigger release).
4. **Build the bundle**: `./gradlew :androidApp:bundleRelease` →
   `androidApp/build/outputs/bundle/release/androidApp-release.aab`. It refuses to build without the
   release keystore properties (README "Signing a release build").
5. **Play Console → Test and release → Testing → Internal testing → Create new release** → upload
   the `.aab` → release name (defaults to the versionName) → release notes in English and Hungarian
   → **Next → Save and publish**.
6. **Testers** (first time only): Internal testing → **Testers** tab → create an e-mail list with
   your own Google account → copy the **opt-in link**, open it on the phone, accept, then install
   from the Play Store link on that page. Uninstall your sideloaded debug/qa build first if the
   package names collide (`com.levelchef.android` — the `qa` build is `.qa` and can stay).
7. Later, to go wider: on the release, **Promote release → Closed testing** (and, once production
   access is granted, **Production**). Production releases can be rolled out to a percentage of users
   first (staged rollout) and halted if a problem shows up.

## Open items

- **In-app "report this recipe"** for AI-generated content (Play generative-AI policy).
- Publish the privacy policy and terms of use ([`TERMS.md`](TERMS.md)) at public URLs, and link
  both from Settings.
- First-release DB policy flip: from the first production release on, schema changes need
  migrations (AGENTS.md "Not yet done" #2).
