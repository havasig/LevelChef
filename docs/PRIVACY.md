# Privacy — policy draft and Play Console "Data safety" answers

> **Status: draft for the maintainer to review before the first Play release.** It describes what
> the code in this repository actually does; it is not legal advice. Publish the policy section at a
> public URL (Google Play requires one) and keep it in sync with the code.

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

- **Google Gemini API terms:** on the *free tier*, Google may use prompts and responses to improve
  its products, and human reviewers may read them. The prompt contains the user's diet and
  allergens. **Before a public release, enable billing on the Gemini project** (paid-tier data is
  not used to improve Google's products) — or disclose free-tier use explicitly in the policy.
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
| Is it shared with third parties? | Diet/allergens are sent to Google (Gemini API) as a **service provider** processing on the developer's behalf — under Play's definitions that is "collected", not "shared", when the processor only acts for the developer (paid tier). On the free tier, treat it as **shared**. |
| Is the data processed ephemerally? | Yes for the proxy (only counters are kept); Gemini per Google's terms |
| Is data encrypted in transit? | **Yes** (HTTPS only) |
| Can users request deletion? | **Yes** — in-app (Settings → Delete account) |
| Purpose | **App functionality** (personalised recipe recommendations); the device id: **Fraud prevention, security** (rate limiting) |

## Policy text (template)

> **LevelChef privacy policy** — last updated: _date_
>
> LevelChef stores your cooking data only on your device. To suggest recipes, the app sends your
> onboarding answers — cooking level, diet, food allergies, favourite cuisines, spice tolerance,
> goal, available cooking time and household size — and the app language to our server, which
> asks Google's Gemini service to generate recipes. We do not send your name, contacts, location
> or any other personal information, and we don't use advertising or analytics. Our server keeps
> only anonymous daily request counts (with a random app-install id and your IP address) for up to
> two days to prevent abuse. Android may include your LevelChef data in your device backup. You can
> delete all your data at any time in Settings → Delete account. Contact: _email_.
