# Manual QA — regression test script

A step‑by‑step script for a human tester. No knowledge of the codebase is required —
follow the steps, compare against the **bold expected result**, and log pass/fail.

For architecture see [`AGENTS.md`](../AGENTS.md); for the Git/CI workflow see
[`CONTRIBUTING.md`](../CONTRIBUTING.md); for tooling see [`TOOLING.md`](TOOLING.md).

> **Contributors:** this file is part of the definition of done. Any PR that adds or changes
> user‑visible behaviour updates this script in the same PR — see
> [Extending this script](#extending-this-script) at the bottom.

_Last updated: 2026-09-23 · covers through the Gemini-backed recipe recommender, the pre-release schema reset to v1, the full delete-account wipe, the one-time pantry seed and Trophies refreshing on return._

---

## 1. How to use this script

- **Who:** a tester with an Android device or emulator.
- **When:**
  - **Smoke pass** (the **P0** scenarios, ~10 min) — on every feature PR before it merges, and on every release candidate.
  - **Full pass** (all scenarios, ~40 min) — on every release candidate, and after any change to navigation, the theme/locale plumbing, or the database schema.
- **How:** work top to bottom. Each scenario lists **Priority**, **Preconditions**, numbered **Steps**, and the **expected** outcome inline (bold). Do the preconditions first.
- **Recording results:** copy the [results log](#results-log) into the release ticket (or a spreadsheet) and fill in Pass / Fail / Blocked + notes for every scenario you ran. Note the build, device model and Android version at the top.
- **Filing bugs:** if actual ≠ expected, open an issue titled with the scenario id, e.g. `SM-06: cooking-session count not updated on Home after "I made it"`. Attach a screen recording and the relevant `logcat` (see below).

### Priorities

| Tag | Meaning |
|---|---|
| **P0** | Core user journey. Part of every smoke pass. A P0 failure blocks the release. |
| **P1** | Important but not on the critical path. Run in a full pass. |

---

## 2. Test environment

### Build & install

```bash
./gradlew :androidApp:installDebug        # installs the debug build on the connected device
```

### Device matrix

Run the full pass on at least:

- **Android 8.0 (API 26)** — the `minSdk`. Emulator is fine.
- **A current Android** (14/15) — a physical device if available.

Smoke passes can run on a single device.

### Install states

Two states behave differently and several scenarios call one out explicitly:

- **Fresh install** — no prior data. Reset with:
  ```bash
  adb shell pm clear com.levelchef.android
  ```
- **Upgrade install** — install the *previous* released build, use it, then install the new build **over it** without clearing. This is the only way to exercise the SQLDelight schema migration (SM-14). **Not possible until the first release** — see SM-14.

### Themes

The app follows the OS light/dark setting (plus an in‑app override — SM-09). Run the P0 scenarios once with the system in **light** and once in **dark**. Toggle via the OS quick settings or `adb shell "cmd uimode night yes|no"`.

### Typeface

All text now renders in **Inter** (previously the platform default). No dedicated scenario — while
running any of the scenarios below, flag anything that looks like the system font instead (a sign
the font resource failed to load) or new text wrapping/truncation versus a prior build.

### Recipe recommendations

Home's "Recommended for you" list, and every recipe the Recipes tab and recipe detail resolve by
id, are generated from your survey answers by the Gemini API when the build has `GEMINI_API_KEY`
set in `local.properties`. With a key configured, the exact recipes, their count and their content
will vary between builds/testers and change whenever you retake the survey (SM-11) — that's
expected, not a bug. **Most test builds won't have a key set** and will instead see the fixed
bundled fallback set of 3 recipes ("Chicken curry with coconut milk", "Steak quinoa bowl", "Jucy
pasta") that the scenarios below were written against; note in the results log which case you ran.

### Logs & crashes

There is **no in‑app crash screen** by design — an unhandled error is logged and then the process exits to the OS. Keep a logcat open while testing:

```bash
adb logcat -s LevelChef:V AndroidRuntime:E
```

A crash = an `AndroidRuntime` fatal exception and/or the app disappearing. Always attach this output to a crash bug.

---

## 3. Scenarios

### SM-01 · First launch & mandatory onboarding

**Priority:** P0 · **Preconditions:** fresh install.

1. Launch the app.
   - **The onboarding survey is shown immediately** — no bottom navigation bar, no visible way to skip it.
2. Press the system Back button on the first question.
   - **The app does not advance to Home** — the survey gate holds.
3. Work through every step of the wizard (cooking level → … → household size), choosing an answer on each.
   - **Each step requires a selection before "Next" is enabled**; the step/progress indicator advances.
4. Complete the final step.
   - **The survey closes and the Home screen appears**, now with the bottom navigation bar.
5. Swipe the app away from Recents, then relaunch it.
   - **The app opens directly to Home** — the survey is not shown again.

### SM-02 · Home dashboard content

**Priority:** P0 · **Preconditions:** onboarding complete.

1. Look at the Home screen.
   - **Top bar** shows the app title and a **gear icon** on the right.
   - **Visible:** a level badge, an XP progress bar with an "X / Y XP" caption, two stat cards (**"🍳 N"** cooking sessions and **"🌿 N ingredients tried"**), a weekly‑challenge card, a "Cook today" button, and a **"Recommended for you"** list of recipe cards (count/content vary — see [Recipe recommendations](#recipe-recommendations) above).
   - On a fresh install there is **no "Last cooked" card** yet, and **"ingredients tried" is 0** — the
     seeded starter pantry doesn't count; only ingredients you add yourself do.
2. Tap the **"🌿 ingredients tried"** stat card.
   - **Navigates to the Ingredients (pantry) list.** Go back.
3. Tap the **gear** icon.
   - **Navigates to Settings.** Go back.
4. Look at the weekly‑challenge card.
   - **Shows the current week's real challenge** (title + "+N XP" badge) — one of a rotating catalog
     of 9, picked deterministically per calendar week, so the exact title/target varies by when you
     test. The status line reads **"In progress"** until its condition is met.
   - The **"Done"** button is **disabled** until the challenge's condition is actually met (e.g. log
     enough cooking sessions, per the challenge's own description) — tapping it before then is
     expected to do nothing, since it's disabled.
   - Once eligible, tap **"Done"**: **the card switches to a "Completed" status, the button
     disappears, and the XP badge amount is added to your total XP** (check the level‑progress bar
     above). Reloading Home (background/foreground, or the ON_RESUME refresh) keeps it **"Completed"**.
5. Tap the **"Cook today — show me a recipe!"** button.
   - **Navigates to Recipe Detail** for one of the "Recommended for you" recipes — picked at
     random each tap, so it won't always be the same one (back out and tap a few more times to
     see it vary; two taps in a row landing on the same recipe is expected, not a bug).
   - Go back — **returns to Home.**

### SM-03 · Recipe detail — open from Home

**Priority:** P0 · **Preconditions:** onboarding complete.

1. On Home, tap the first recommendation card (with the fallback set: "Chicken curry with coconut
   milk" — see [Recipe recommendations](#recipe-recommendations)).
   - **Opens the Recipe Detail screen:** back arrow + the recipe name as the title + a gear icon; **no bottom navigation bar**.
   - **Contains, top to bottom:** an emoji hero tile with a **"+XP" badge** (value matches the card), the recipe name, a row of tags (time / protein / difficulty / "N ingredients"), a 4‑cell macro grid (Calories / Protein / Carbs / Fat), a "Set servings" control, an "Ingredients" checklist, numbered "Steps", a "Related video" row, an "I made it" button and a "Save" button.
2. Tap the **gear** icon.
   - **Opens Settings.** Press Back.
   - **Returns to the same Recipe Detail screen** (not Home).
3. Tap the **back arrow**.
   - **Returns to Home.**
4. Open each recommendation card in turn.
   - **Each shows its own name, emoji, XP, ingredients and steps** — never a stale/previous recipe.

### SM-04 · Recipe detail — servings stepper & ingredient checklist

**Priority:** P0 · **Preconditions:** a recipe detail screen open.

1. Note an ingredient that has a quantity, e.g. "300 g chicken thigh", and the servings count (starts at 2).
2. Tap **+** on "Set servings" several times.
   - **The count increases and the ingredient quantities scale up proportionally** (300 g → 450 g → 600 g …).
   - **The count stops at 12** and does not go higher.
3. Tap **−** several times.
   - **Count and quantities scale back down; the count stops at 1** (never 0 or negative).
   - **Ingredients with no quantity** (e.g. "Cilantro, lime, salt") **never change.**
4. Tap several ingredient rows.
   - **Each row independently toggles a checkmark and a strike‑through.**
5. Go Back to Home, then reopen the same recipe.
   - **The checklist is cleared and the servings count is back to the recipe default** (this state is intentionally not saved).

### SM-05 · Recipe detail — "Save" bookmark persists

**Priority:** P0 · **Preconditions:** a recipe detail screen open.

1. Tap **Save**.
   - **The button changes to "Saved"** and a short confirmation message ("Recipe saved") appears and disappears on its own (~2–3 s).
2. Go Back to Home, then reopen the same recipe.
   - **The button still shows "Saved".**
3. Swipe the app away from Recents, relaunch, and navigate back to that recipe.
   - **Still "Saved"** (the bookmark is stored in the database).
4. Tap **"Saved"** to unsave.
   - **Reverts to "Save"**, with a "Removed from saved" message. Reopen the recipe → **still unsaved**.
5. Save recipe A; leave recipe B untouched. Open recipe B.
   - **Recipe B shows "Save", not "Saved"** — the state is per recipe.

### SM-06 · Recipe detail — "I made it" → Meal Review → Home stats

**Priority:** P0 · **Preconditions:** a recipe detail screen open; note Home's cooking‑session count and XP first.

1. Note the recipe's XP reward (the "+XP" badge). Tap **"I made it"**.
   - **Navigates to the "Log experience" (Meal Review) screen** — back arrow + title, **no bottom navigation bar**; nothing is logged yet.
   - **The recipe name and "+XP" badge match**, and the macro fields (Calories/Protein/Carbs/Fat) are **pre‑filled from the recipe's own values**; **Save is disabled** (dimmed).
2. Tap a star to rate the meal (see **SM-17** for the screen's own controls in detail), then tap **Save**.
   - **Navigates back to Home** (not to the recipe) — both the recipe detail and Meal Review screens are popped off the back stack.
3. On Home:
   - **The "🍳" cooking‑sessions count is 1 higher.**
   - **The XP value / progress bar has increased by the recipe's reward.**
   - **A "Last cooked" card now appears** showing this recipe.
   - If the reward crossed a level threshold, **the level badge/label updates too.**
4. Reopen the recipe, tap "I made it", rate it, and Save again.
   - **The count increases again** (repeat cooks are allowed).
5. Tap "I made it", then press the **back arrow** on Meal Review without tapping Save.
   - **Returns to Recipe Detail; nothing is recorded** — Home's stats are unchanged.

### SM-07 · Recipe detail — timer chip & related video

**Priority:** P1 · **Preconditions:** a recipe with a timed step open (e.g. the curry, step 4).

1. Find a step showing a **"Start N min timer"** chip. Tap it.
   - **The chip switches to a live "MM:SS" countdown** ticking down once per second, with a close
     icon in place of the play icon.
   - Tap the running chip again.
   - **It cancels: the countdown stops and the step reverts to the idle "Start N min timer" chip.**
2. Start a timer, then tap a **different** step's timer chip before the first finishes.
   - **The first step's chip reverts to idle and the newly-tapped step's chip starts counting down
     instead** — only one timer runs at a time.
3. Start a short timer and leave it running until it reaches 0:00.
   - **A "Timer done!" message appears in the snackbar**, and the chip reverts to idle.
4. Tap the **"Related video"** row.
   - **The device leaves the app and opens the link** in a browser / YouTube.
5. Return to the app.
   - **The recipe detail screen is still there, unchanged** — any timer that was running when you
     left is still ticking (it lives in the screen's ViewModel, not tied to being visible).

### SM-08 · Ingredients / pantry — browse, add, edit, delete

**Priority:** P0 for *add* and *delete*; P1 otherwise · **Preconditions:** onboarding complete.

1. Open the Ingredients list (Home → "ingredients tried" card).
   - **On a fresh install the list is pre‑populated** (~18 items) **and grouped by category** (Meat, Dairy, Vegetables, …). No bottom bar.
2. In a category that has a **"Show all"** link, tap it, then **"Show less"**.
   - **The extra rows expand and collapse.**
3. Tap an ingredient.
   - **Opens its detail** (emoji, name, category tag, unit, and macros if set).
4. Tap **Edit**, change the name, tap **Save**.
   - **Returns to the detail with the new name;** the list shows it updated too.
5. On the detail, tap **Delete** and confirm in the dialog.
   - **Returns to the list; the item is gone.**
6. From the list, tap **＋**, enter a name, pick a category and unit, optionally enter macros, tap **Save**.
   - **The new item appears in the correct category group**, with an emoji derived from the category.
7. Try to save the form with a **blank name**.
   - **Save is blocked / a validation error is shown.**
8. Swipe the app away, relaunch, reopen the list.
   - **Your add / edit / delete all persisted;** the default items are **not** re‑added.
9. Delete **every** ingredient, swipe the app away, relaunch, reopen the list.
   - **The list stays empty** — the default items are seeded only once, not whenever the pantry is empty.

### SM-09 · Settings — theme

**Priority:** P0 · **Preconditions:** onboarding complete.

1. Home → gear → Settings.
   - **No bottom bar; a back arrow is present.**
2. Choose **Light**.
   - **The UI switches to light immediately** — no restart needed.
3. Choose **Dark**, then **System**.
   - **Dark applies immediately; System follows the current OS setting.**
4. Set **Dark**, swipe the app away, relaunch.
   - **The app reopens in Dark** — the choice is remembered.
5. Navigate around (Home, a Recipe Detail, Ingredients).
   - **The chosen theme is applied consistently on every screen.**

### SM-10 · Settings — language (Hungarian)

**Priority:** P0 · **Preconditions:** onboarding complete; app currently in English.

1. Settings → Language → **Magyar**.
   - **The screen reloads and Settings labels are now in Hungarian.**
2. Navigate to Home, the Ingredients list, and a **Recipe Detail**.
   - **All visible text is Hungarian.** Spot‑check the recipe detail: **"Adagok beállítása"**, **"Hozzávalók"**, **"Lépések"**, **"Elkészítettem"**, **"Mentés"**; the confirmation messages are localised too.
3. Tap "Elkészítettem" to open Meal Review.
   - **Spot‑check:** **"Élmény naplózása"** (title), **"ÉRTÉKELÉS"**, **"JEGYZET"**, **"MAKRÓ ÉRTÉKEK"**, **"HOZZÁVALÓK"**, **"Mentés"**.
4. Open the **Recipes** tab.
   - **Spot‑check:** **"Mentett receptjeim"** (title), **"Receptek keresése…"** (search placeholder),
     **"Összes" / "Elkészítve" / "Új"** (tabs), **"Legutóbb elkészítve"** (last‑cooked label).
5. On Android 13+, open the OS **Settings → Apps → LevelChef → Language**.
   - **LevelChef is listed with a per‑app language override.**
6. Switch back to **English** in‑app, swipe the app away, relaunch.
   - **The language choice persisted across the restart.**

### SM-11 · Settings — retake the survey

**Priority:** P0 · **Preconditions:** onboarding already completed once.

1. Settings → **"Retake the survey"** (or similarly named).
   - **The onboarding wizard opens at step 1 (cooking level)** — *not* at the last step or a summary.
2. Complete the wizard again, changing at least one answer (e.g. dietary preference).
   - **Returns to the app** with the new answers stored.
   - If `GEMINI_API_KEY` is configured (see [Recipe recommendations](#recipe-recommendations)),
     **Home's "Recommended for you" list regenerates to reflect the new answers** the next time it
     loads (may take a moment); without a key, the fallback set is unchanged.
3. If the wizard allows Back/cancel partway, do that.
   - **The app returns to a normal onboarded state;** the previous response is not lost.

### SM-12 · Settings — developer: clear onboarding storage

**Priority:** P1 · **Preconditions:** onboarding completed; **debug build**.

1. Settings → developer section → **"Clear onboarding storage"**.
2. Swipe the app away and relaunch.
   - **The onboarding survey is shown again** (the gate re‑triggers because no survey response is stored).
   - **Home stats, pantry, and saved recipes are unaffected.**
3. On a **release build**, open Settings.
   - **There is no Developer section.**

### SM-13 · Navigation chrome & back stack

**Priority:** P0 · **Preconditions:** onboarding complete.

1. Check where the **bottom navigation bar** appears.
   - **Visible only on Home, Recipes, and Trophies.**
2. Drill into Recipe Detail, Meal Review ("I made it"), Ingredients (list → detail → form), Settings, and the onboarding wizard.
   - **The bottom bar is hidden on all of these**; each screen draws its own top bar; **content is not clipped by the status bar or the gesture‑nav bar.**
3. Recipe Detail → gear → Settings → Back → Back.
   - **Lands on Home via Recipe Detail** — the back stack is intact, no screens skipped, no double‑press needed.
4. Recipe Detail → "I made it" → Meal Review → Save.
   - **Lands directly on Home** — Recipe Detail and Meal Review are both popped, so Back from Home does not return to either.
5. Switch between the Home / Recipes / Trophies tabs repeatedly.
   - **Each tab restores its previous state;** no duplicate stacking of a tab.
6. Rotate the device on Recipe Detail and on the Ingredients list.
   - **No crash;** scroll position is roughly kept; **layout still clears the system bars.**
7. Recipes tab → tap a saved recipe → Back.
   - **Opens Recipe Detail, then returns to the Recipes tab** with the bottom bar visible again — see
     **SM-18**.

### SM-14 · Database schema migration

**Priority:** P0 · **Preconditions:** **upgrade install** (see [Install states](#install-states)).

> **Skipped until the first release.** Nothing is installed outside development yet, so the schema
> was reset to a single v1 baseline with no migrations. Mark this row *Skipped* until a previous
> release exists. Dev devices holding a pre-reset database must clear app data once
> (`adb shell pm clear com.levelchef.android`) — Android refuses to open a database whose version
> is newer than the app's schema.

1. Install the **previous** release. Complete onboarding, log a cook (SM-06), save a recipe (SM-05),
   earn a badge (SM-19) and add a pantry item (SM-08).
2. Install **this** build over it — **do not** clear data.
3. Launch the app.
   - **Opens straight to Home** (survey not shown again).
   - **Cooking‑session count, XP, "Last cooked", saved recipes, earned badges and pantry items are all still there.**
4. Exercise whatever the new schema adds (the migration's PR lists it) and relaunch.
   - **No crash; the new data persists.** For the `generatedRecipe` table: if `GEMINI_API_KEY` is
     configured (see [Recipe recommendations](#recipe-recommendations)), load Home on the previous
     release so a recommendation batch is cached, then upgrade — **the same recommendations still
     resolve by id from the Recipes tab / recipe detail after the upgrade.**
5. Empty the pantry (SM-08 step 9), swipe the app away, relaunch.
   - **The pantry stays empty** — an upgraded install is not re‑seeded.

### SM-15 · Process death & configuration changes

**Priority:** P1 · **Preconditions:** onboarding complete.

1. In Developer Options, enable **"Don't keep activities"**. Open a Recipe Detail, change servings and tick some ingredients, switch to another app, then return.
   - **The screen restores without crashing.** (Servings/checklist resetting to defaults is acceptable.)
2. Toggle the theme in Settings, then rotate the device several times quickly.
   - **No crash; the theme stays consistent.**
3. Turn on airplane mode, open a Recipe Detail, tap **"Related video"**.
   - **The app does not crash;** the browser shows its own offline error.
4. Disable "Don't keep activities" when done.

### SM-16 · Placeholders & the hidden design‑system showcase

**Priority:** P1 · **Preconditions:** onboarding complete.

1. Bottom nav → **Trophies** tab.
   - **Shows the Trophy Room** (see **SM-19**) — no screen is a placeholder any more.
2. On a **debug build**, tap the **Home** bottom‑nav item **5 times quickly**.
   - **The hidden Design System showcase opens.** Back returns to Home.
   - A slow tap, or tapping a different tab first, **resets the counter** (a normal tap on Home just goes Home).
3. On a **release build**, tap Home 5 times quickly.
   - **Nothing opens** — Home just stays on Home.

### SM-17 · Meal Review ("Log experience") screen controls

**Priority:** P0 · **Preconditions:** reached via Recipe Detail → "I made it" (see SM-06).

1. Look at the screen.
   - **Sections top to bottom:** recipe name + "+XP" badge, "RATING" (5 stars + "N/5"), "NOTE" (placeholder "Describe your experience…"), a bare cook‑time stepper ("N min"), "MACRO VALUES" (Calories/Protein/Carbs/Fat, each a colored label + a −/value/+ stepper), "INGREDIENTS" (a static checklist matching the recipe), and **Save**.
   - **Save is disabled** until a star is tapped.
2. Tap the 3rd star.
   - **Stars 1–3 fill in, 4–5 stay outline; the "N/5" text reads "3/5"; Save becomes enabled.**
3. Type a few words in the Note box.
   - **The placeholder disappears and the typed text shows.**
4. Tap **+** / **−** on the cook‑time row and on each macro row.
   - **Each value changes independently by its own step size and never goes below 0.**
5. Tap **Save**.
   - **Navigates back to Home** (see SM-06 step 3 for the resulting stats change).

### SM-18 · Recipes tab — "My saved recipes"

**Priority:** P0 · **Preconditions:** onboarding complete; save at least 2 recipes (SM-05) and cook
one of them (SM-06) first.

1. Bottom nav → **Recipes** tab.
   - **Shows "My saved recipes"** — a search bar, an **All / Cooked / New** tab row, and your saved
     recipes below. No back arrow; the bottom bar stays visible (it's a top‑level tab).
   - **The recipe you cooked shows as a "Last cooked" card** (star rating + a checkmark); **the
     others show as a plain card** ("+XP", "⏱ N min · Difficulty").
2. Type part of a saved recipe's name into the search bar.
   - **The list narrows to matching recipes only**, live as you type.
3. Clear the search, then switch to the **Cooked** tab, then **New**.
   - **Cooked** shows only the recipe(s) you've cooked; **New** shows only the ones you haven't.
   - **All** shows the full list again.
4. Tap the delete icon on a saved recipe.
   - **It disappears from the list immediately.**
5. Open that same recipe from Home or search, and check its **Save** button.
   - **It shows "Save", not "Saved"** — deleting from the Recipes tab un‑saves it (does not delete
     any cooking‑session history).
6. Tap a saved recipe's card.
   - **Opens Recipe Detail** for that recipe. Press Back.
   - **Returns to the Recipes tab**, bottom bar visible, list and tab selection unchanged.

### SM-19 · Trophies tab — stats refresh on return

**Priority:** P1 · **Preconditions:** onboarding complete.

1. Open the **Trophies** tab and note the XP, kitchen time and cooking-session numbers.
2. Switch to **Home**, open a recipe, tap **"I made it"**, set a rating and a duration, tap **Save**.
3. Switch back to **Trophies**.
   - **XP, kitchen time and the session count already include the new cook** — no app restart needed.
   - Any badge the cook completed (e.g. *First Bite*) **shows as earned**.

### SM-20 · Settings — delete account wipes everything

**Priority:** P0 · **Preconditions:** onboarding complete; at least one logged cook (SM-06), one saved
recipe (SM-05), one earned badge (SM-19) and a custom pantry item (SM-08).

1. Settings → **Delete account** → confirm.
   - **A success message shows, then the onboarding survey appears.**
2. Complete the survey again.
   - **Home shows Level 1 / 0 XP, 0 cooking sessions and no "Last cooked".** Weekly-challenge XP from before is gone too.
   - **Recipes tab is empty** — no saved recipes left.
   - **Trophies shows no earned badges.**
   - **The pantry holds exactly the default starter items** — your custom item is gone and the defaults are back.
   - **Theme and language are back to System.**

---

## 4. Results log

Copy into the release ticket. One row per scenario you ran.

```
Build / commit: __________     Device: __________     Android: __________     Tester: __________     Date: __________

| Scenario | P0? | Light | Dark | Result (Pass/Fail/Blocked/Skipped) | Notes / bug link |
|----------|-----|-------|------|------------------------------------|------------------|
| SM-01 First launch & onboarding        | P0 |  |  |  |  |
| SM-02 Home dashboard                    | P0 |  |  |  |  |
| SM-03 Recipe detail — open from Home    | P0 |  |  |  |  |
| SM-04 Recipe detail — servings & checks | P0 |  |  |  |  |
| SM-05 Recipe detail — Save persists     | P0 |  |  |  |  |
| SM-06 Recipe detail — I made it         | P0 |  |  |  |  |
| SM-07 Recipe detail — timer & video     | P1 |  |  |  |  |
| SM-08 Ingredients CRUD                  | P0 |  |  |  |  |
| SM-09 Settings — theme                  | P0 |  |  |  |  |
| SM-10 Settings — language               | P0 |  |  |  |  |
| SM-11 Settings — retake survey          | P0 |  |  |  |  |
| SM-12 Settings — clear onboarding       | P1 |  |  |  |  |
| SM-13 Navigation & back stack           | P0 |  |  |  |  |
| SM-14 Schema migration (upgrade)        | P0 |  |  |  |  |
| SM-15 Process death & rotation          | P1 |  |  |  |  |
| SM-16 Placeholders & showcase           | P1 |  |  |  |  |
| SM-17 Meal Review controls              | P0 |  |  |  |  |
| SM-18 Recipes tab — saved recipes       | P0 |  |  |  |  |
| SM-19 Trophies — refresh on return      | P1 |  |  |  |  |
| SM-20 Settings — delete account         | P0 |  |  |  |  |
```

**Release exit criteria:** every **P0** scenario Pass in both light and dark; **SM-14** Pass on an upgrade install (once a previous release exists); **zero** crashes in any scenario.

---

## Extending this script

**Every PR that adds or changes user‑visible behaviour updates this file in the same PR.** It is
listed as a "Working practice" in [`AGENTS.md`](../AGENTS.md) and in the
[`CONTRIBUTING.md`](../CONTRIBUTING.md) pre‑push checklist. The `new-feature-screen` skill has it as
its final step.

Use this checklist when you touch the app:

- [ ] **New screen or flow** → add a new `SM-NN` scenario. Give it a **Priority**, **Preconditions**, numbered **Steps**, and a **bold expected** result for each step. Add a row to the [results log](#results-log).
- [ ] **Changed existing behaviour** → update the affected scenario's steps and expected results. Don't leave stale expectations.
- [ ] **New persisted data or a schema migration** → extend **SM-14** with an upgrade check for the new data (save something in the old build, confirm it survives the upgrade).
- [ ] **New setting / toggle** → add steps that change it, verify the effect on other screens, and kill+relaunch to verify it persists.
- [ ] **New navigation entry point or back path** → extend **SM-13** (bottom‑bar visibility, back‑stack order, system‑bar insets).
- [ ] **New user‑facing strings** → extend **SM-10** with a Hungarian spot‑check phrase from the new screen. Confirm `values-hu/strings.xml` was added.
- [ ] **Removed a feature** → mark its scenario `**[Removed in #NN]**` at the top and delete the steps; don't renumber anything else.
- [ ] Mark a scenario **P0** only if it sits on the primary journey: onboarding → Home → open a recipe → cook it. Everything else is **P1**.
- [ ] Bump the `_Last updated_` line at the top (date + PR number).

**Never renumber existing scenarios** — ids are referenced in bug reports and release tickets. Only append.
