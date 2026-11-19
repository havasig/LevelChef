# Privacy — policy and Play Console "Data safety" answers

> It describes what the code in this repository actually does; it is not legal advice. Published at
> <https://havasig.github.io/LevelChef/policy/privacy.html> (`docs/policy/privacy.html`, GitHub Pages
> serving `docs/` on `main`) — keep that page in sync with this file and with the code.

## What the app stores, and where

| Data | Where | Leaves the device? |
|---|---|---|
| Onboarding answers: cooking level, diet, **allergens**, cuisines, spice tolerance, goal, weeknight time, household size | SQLite on the device | **Yes** — sent to the recipe proxy to generate recommendations (see below) |
| Cooking history (recipe, date, rating, note, cook time, macros), saved recipes, pantry, badges, weekly-challenge progress | SQLite on the device | No (only via Android's own backup, below) |
| Generated recipes (cache) | SQLite on the device | No |
| Theme and language choice | SharedPreferences | No |
| A random per-install id (UUID) | SharedPreferences | Yes — sent as `X-Device-Id` to meter the daily quota; excluded from backup |

There are no accounts, no analytics or advertising SDKs, and no crash-reporting SDK
(`CrashLogWriter` is a seam with nothing wired to it).

## The recipe proxy and Gemini

To recommend recipes the app sends the onboarding answers above (as fixed values such as
`VEGAN`, `NUTS` — never free text) and the app language to the LevelChef recipe proxy, a Cloudflare
Worker run by the developer. The proxy adds the request's IP address to its daily rate-limit
counters (kept up to 2 days in Cloudflare KV), builds a prompt from the answers and forwards it to
Google's Gemini API. The install id and IP address are **not** forwarded to Google.

- **Google Gemini API terms — the project uses the free tier (decision: no billing).** On the free
  tier Google may use prompts and responses to improve its products, and human reviewers may read
  them. The prompt contains the user's diet and allergens (never a name or contact detail), so the
  policy text below **says so explicitly**, and the Data safety form declares it as **shared**.
  Enabling billing later would remove this, and the two disclosures could then be relaxed.
- **Cloudflare** processes the request (including the IP address) as the proxy's host.

## Backup

`android:allowBackup` is on, so Android's backup (Google account / device transfer) can copy the
database and settings — a restored phone keeps its cooking history. The per-install id is excluded
(`res/xml/data_extraction_rules.xml`, `backup_rules.xml`).

## Deletion

Settings → **Delete account** deletes every piece of the data above from the device, including the
generated-recipe cache, and resets theme and language. Nothing is stored server-side beyond the
proxy's anonymous daily counters, which expire on their own after 2 days.

## Play Console "Data safety" — suggested answers

| Question | Answer |
|---|---|
| Does the app collect or share user data? | **Yes** |
| Data types collected | **Health and fitness → Health info** (diet, allergens — Google classifies dietary restrictions/allergies as health info); **App info and performance → Other** is *not* collected; **Device or other IDs** (the per-install UUID) |
| Is it shared with third parties? | **Yes — Health info is shared with Google.** On the Gemini free tier Google may use prompts for its own product improvement, so it is not acting purely as a service provider (on a paid tier it would be "collected", not "shared"). |
| Is the data processed ephemerally? | Yes for the proxy (only counters are kept); Gemini per Google's terms |
| Is data encrypted in transit? | **Yes** (HTTPS only) |
| Can users request deletion? | **Yes** — in-app (Settings → Delete account) |
| Purpose | **App functionality** (personalised recipe recommendations); the device id: **Fraud prevention, security** (rate limiting) |

## Policy text

Published at [`docs/policy/privacy.html`](policy/privacy.html) —
<https://havasig.github.io/LevelChef/policy/privacy.html>. Contact: levelchef.app@gmail.com.
