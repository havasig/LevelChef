# Manual QA — regression test script

A step‑by‑step script for a human tester. No knowledge of the codebase is required —
follow the steps, compare against the **bold expected result**, and log pass/fail.

For architecture see [`AGENTS.md`](../AGENTS.md); for the Git/CI workflow see
[`CONTRIBUTING.md`](../CONTRIBUTING.md); for tooling see [`TOOLING.md`](TOOLING.md).

> **Contributors:** this file is part of the definition of done. Any PR that adds or changes
> user‑visible behaviour updates this script in the same PR — see
> [Extending this script](#extending-this-script) at the bottom.

_Last updated: 2026-09-06 · covers through PR #36 (recipe detail screen)._

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
- **Upgrade install** — install the *previous* released build, use it, then install the new build **over it** without clearing. This is the only way to exercise the SQLDelight schema migration (SM-14).

### Themes

The app follows the OS light/dark setting (plus an in‑app override — SM-09). Run the P0 scenarios once with the system in **light** and once in **dark**. Toggle via the OS quick settings or `adb shell "cmd uimode night yes|no"`.

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
   - **Visible:** a level badge, an XP progress bar with an "X / Y XP" caption, two stat cards (**"🍳 N"** cooking sessions and **"🌿 N ingredients tried"**), a weekly‑challenge card, a "Cook today" button, and a **"Recommended for you"** list of 3 recipe cards.
   - On a fresh install there is **no "Last cooked" card** yet.
2. Tap the **"🌿 ingredients tried"** stat card.
   - **Navigates to the Ingredients (pantry) list.** Go back.
3. Tap the **gear** icon.
   - **Navigates to Settings.** Go back.
4. *(Known limitations — do not raise as bugs)* the **"Cook today"** button and the weekly‑challenge **"Done"** button currently do nothing, and the challenge card shows static placeholder text.

### SM-03 · Recipe detail — open from Home

**Priority:** P0 · **Preconditions:** onboarding complete.

1. On Home, tap the first recommendation card ("Chicken curry with coconut milk").
   - **Opens the Recipe Detail screen:** back arrow + the recipe name as the title + a gear icon; **no bottom navigation bar**.
   - **Contains, top to bottom:** an emoji hero tile with a **"+XP" badge** (value matches the card), the recipe name, a row of tags (time / protein / difficulty / "N ingredients"), a 4‑cell macro grid (Calories / Protein / Carbs / Fat), a "Set servings" control, an "Ingredients" checklist, numbered "Steps", a "Related video" row, an "I made it" button and a "Save" button.
2. Tap the **gear** icon.
   - **Opens Settings.** Press Back.
   - **Returns to the same Recipe Detail screen** (not Home).
3. Tap the **back arrow**.
   - **Returns to Home.**
4. Open each of the 3 recommendation cards in turn.
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

### SM-06 · Recipe detail — "I made it" updates Home stats

**Priority:** P0 · **Preconditions:** a recipe detail screen open; note Home's cooking‑session count and XP first.

1. Note the recipe's XP reward (the "+XP" badge). Tap **"I made it"**.
   - **A message appears: "Logged! +<XP> XP earned".**
   - **You stay on the recipe screen** — no navigation happens.
2. Tap Back to return to Home.
   - **The "🍳" cooking‑sessions count is 1 higher.**
   - **The XP value / progress bar has increased by the recipe's reward.**
   - **A "Last cooked" card now appears** showing this recipe.
   - If the reward crossed a level threshold, **the level badge/label updates too.**
3. Reopen the recipe, tap "I made it" again, return to Home.
   - **The count increases again** (repeat cooks are allowed).

### SM-07 · Recipe detail — timer chip & related video

**Priority:** P1 · **Preconditions:** a recipe with a timed step open (e.g. the curry, step 4).

1. Find a step showing a **"Start N min timer"** chip. Tap it.
   - **A message appears: "Step timers are coming soon".**
   - **No countdown starts** — the chip is a visual placeholder for now.
2. Tap the **"Related video"** row.
   - **The device leaves the app and opens the link** in a browser / YouTube.
3. Return to the app.
   - **The recipe detail screen is still there, unchanged.**

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
3. On Android 13+, open the OS **Settings → Apps → LevelChef → Language**.
   - **LevelChef is listed with a per‑app language override.**
4. Switch back to **English** in‑app, swipe the app away, relaunch.
   - **The language choice persisted across the restart.**

### SM-11 · Settings — retake the survey

**Priority:** P0 · **Preconditions:** onboarding already completed once.

1. Settings → **"Retake the survey"** (or similarly named).
   - **The onboarding wizard opens at step 1 (cooking level)** — *not* at the last step or a summary.
2. Complete the wizard again.
   - **Returns to the app** with the new answers stored.
3. If the wizard allows Back/cancel partway, do that.
   - **The app returns to a normal onboarded state;** the previous response is not lost.

### SM-12 · Settings — developer: clear onboarding storage

**Priority:** P1 · **Preconditions:** onboarding completed.

1. Settings → developer section → **"Clear onboarding storage"**.
2. Swipe the app away and relaunch.
   - **The onboarding survey is shown again** (the gate re‑triggers because no survey response is stored).
   - **Home stats, pantry, and saved recipes are unaffected.**

### SM-13 · Navigation chrome & back stack

**Priority:** P0 · **Preconditions:** onboarding complete.

1. Check where the **bottom navigation bar** appears.
   - **Visible only on Home, Recipes, and Trophies.**
2. Drill into Recipe Detail, Ingredients (list → detail → form), Settings, and the onboarding wizard.
   - **The bottom bar is hidden on all of these**; each screen draws its own top bar; **content is not clipped by the status bar or the gesture‑nav bar.**
3. Recipe Detail → gear → Settings → Back → Back.
   - **Lands on Home via Recipe Detail** — the back stack is intact, no screens skipped, no double‑press needed.
4. Switch between the Home / Recipes / Trophies tabs repeatedly.
   - **Each tab restores its previous state;** no duplicate stacking of a tab.
5. Rotate the device on Recipe Detail and on the Ingredients list.
   - **No crash;** scroll position is roughly kept; **layout still clears the system bars.**

### SM-14 · Database schema migration

**Priority:** P0 · **Preconditions:** **upgrade install** (see [Install states](#install-states)).

1. Install the **previous** release. Complete onboarding, log a cook (SM-06), and add a pantry item (SM-08).
2. Install **this** build over it — **do not** clear data.
3. Launch the app.
   - **Opens straight to Home** (survey not shown again).
   - **Cooking‑session count, XP, "Last cooked", and pantry items are all still there.**
4. Open a recipe, tap **Save**, swipe the app away, relaunch, return to that recipe.
   - **Still "Saved"** — confirms the new `savedRecipe` table was added by the migration without wiping the existing data.

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

1. Bottom nav → **Recipes** tab.
   - **Shows a "Recipes — coming next" placeholder** (this is expected, not a bug).
2. Bottom nav → **Trophies** tab.
   - **Shows a placeholder screen.**
3. Tap the **Home** bottom‑nav item **5 times quickly**.
   - **The hidden Design System showcase opens.** Back returns to Home.
   - A slow tap, or tapping a different tab first, **resets the counter** (a normal tap on Home just goes Home).

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
```

**Release exit criteria:** every **P0** scenario Pass in both light and dark; **SM-14** Pass on an upgrade install; **zero** crashes in any scenario.

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
