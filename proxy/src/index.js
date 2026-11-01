// LevelChef recipe proxy — a Cloudflare Worker that sits between the app and the Gemini API.
//
// Why it exists: the Gemini API key must never ship inside the APK (anyone can extract it). The app
// calls this Worker instead; the key lives only in the Worker's secrets, and the Worker enforces the
// usage limits server-side, where a modified client cannot bypass them.
//
// The client never sends a prompt. It sends only the onboarding-survey answers (closed enum values)
// and the app language; the Worker builds the prompt and the response schema itself. That makes the
// endpoint useless as a free general-purpose Gemini relay: the only thing it can ever generate is a
// recipe list.
//
// Limits (all per UTC day, in the RATE_LIMITS KV namespace):
//   - per device (X-Device-Id, a UUID the app generates once)  DEVICE_DAILY_LIMIT,  default 5
//   - per client IP (CF-Connecting-IP)                          IP_DAILY_LIMIT,      default 20
//   - global, across everybody                                  GLOBAL_DAILY_LIMIT,  default 20
//   - upstream attempts per IP, failed ones included            ATTEMPT_DAILY_LIMIT, default 60
// The device/IP/global quota is reserved *before* the upstream call and refunded if it fails, so two
// concurrent requests can't both slip under the limit during Gemini's multi-second latency. KV is
// still not atomic, so the optional BURST rate-limiting binding (see wrangler.toml) caps how many
// requests one IP can have in flight per minute. GLOBAL_DAILY_LIMIT must not exceed the Gemini key's
// own daily quota (the free tier allows 20 requests/day) or the excess users just get 502s.

const GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models";
const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const MAX_BODY_BYTES = 2 * 1024;
const KEY_TTL_SECONDS = 2 * 24 * 60 * 60;
const RECOMMENDATION_COUNT = 5;
const RETRYABLE_UPSTREAM = new Set([500, 503]);
const RETRY_DELAY_MS = 1000;

export const SURVEY_ENUMS = {
  cookingExperience: ["NEVER_COOKED", "BEGINNER", "COMFORTABLE", "CONFIDENT", "PRO"],
  dietaryPreference: ["OMNIVORE", "VEGETARIAN", "VEGAN", "PESCATARIAN", "FLEXITARIAN"],
  spiceTolerance: ["MILD", "MEDIUM", "HOT", "FIRE"],
  cookingGoal: ["MORE_VARIETY", "QUICK_MEALS", "HIGH_PROTEIN", "HEALTHIER_EATING", "BUDGET_FRIENDLY"],
  weeknightTime: ["UNDER_15", "FROM_15_TO_30", "FROM_30_TO_60", "OVER_60"],
  householdSize: ["SOLO", "TWO", "THREE_TO_FOUR", "FIVE_PLUS"],
};
export const SURVEY_SETS = {
  allergens: ["GLUTEN", "DAIRY", "NUTS", "EGGS", "SHELLFISH", "SOY"],
  cuisines: ["ITALIAN", "ASIAN", "MEXICAN", "INDIAN", "MEDITERRANEAN", "AMERICAN", "MIDDLE_EASTERN", "FRENCH"],
};
const LANGUAGES = { en: null, hu: "Hungarian" };

function json(body, status = 200, headers = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json", ...headers },
  });
}

function intVar(value, fallback) {
  const n = Number.parseInt(value, 10);
  return Number.isFinite(n) && n >= 0 ? n : fallback;
}

/**
 * Parses and strictly validates the client body: `{ survey: {…8 fields…}, language: "en" | "hu" }`.
 * Every value must be one of the known enum names; anything else (extra free text, unknown values,
 * duplicates) rejects the request. Returns the normalized survey + language, or null.
 */
export function parseRecipeRequest(raw) {
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch {
    return null;
  }
  const survey = parsed?.survey;
  if (typeof survey !== "object" || survey === null || Array.isArray(survey)) return null;
  const language = parsed.language ?? "en";
  if (typeof language !== "string" || !Object.hasOwn(LANGUAGES, language)) return null;

  const normalized = {};
  for (const [field, allowed] of Object.entries(SURVEY_ENUMS)) {
    const value = survey[field];
    if (typeof value !== "string" || !allowed.includes(value)) return null;
    normalized[field] = value;
  }
  for (const [field, allowed] of Object.entries(SURVEY_SETS)) {
    const values = survey[field] ?? [];
    if (!Array.isArray(values) || values.length > allowed.length) return null;
    if (!values.every((v) => typeof v === "string" && allowed.includes(v))) return null;
    if (new Set(values).size !== values.length) return null;
    normalized[field] = [...values].sort();
  }
  return { survey: normalized, language };
}

/** The recommendation prompt, built only from validated enum values — no client text reaches Gemini. */
export function buildRecipePrompt(survey, language = "en") {
  const list = (values, empty) => (values.length ? values.join(", ") : empty);
  const prompt = `Recommend ${RECOMMENDATION_COUNT} distinct recipes for a home cook with this profile:
- Cooking experience: ${survey.cookingExperience}
- Dietary preference: ${survey.dietaryPreference}
- Allergens to strictly avoid: ${list(survey.allergens, "none")}
- Preferred cuisines: ${list(survey.cuisines, "no strong preference")}
- Spice tolerance: ${survey.spiceTolerance}
- Primary cooking goal: ${survey.cookingGoal}
- Available weeknight cooking time: ${survey.weeknightTime}
- Household size: ${survey.householdSize}

Hard constraints — every recipe MUST respect these, no exceptions:
- Match the dietary preference exactly (e.g. VEGAN means no animal products at all).
- Contain none of the listed allergens, in any ingredient.

Strong preferences — follow these unless they conflict with a hard constraint above:
- Favor the preferred cuisines and match the stated spice tolerance.
- Keep total time within the stated weeknight time budget.
- Scale the \`servings\` field to the household size (SOLO=1, TWO=2, THREE_TO_FOUR=4, FIVE_PLUS=6).
- Lean into the stated cooking goal (e.g. HIGH_PROTEIN favors protein-forward recipes,
  QUICK_MEALS favors the shortest times, BUDGET_FRIENDLY favors inexpensive ingredients).
- Match the difficulty to the stated cooking experience (NEVER_COOKED/BEGINNER → EASY recipes).

For each recipe, provide:
- id: a lowercase, URL-safe slug unique among the ${RECOMMENDATION_COUNT} recipes (e.g. "lemon-garlic-chicken")
- name: a short, appealing title
- emoji: exactly one emoji that best represents the dish
- xpReward: an int from 30-150 reflecting how involved the recipe is (simple=lower, complex=higher)
- timeMinutes: total active + cook time
- difficulty: one of EASY, MEDIUM, HARD
- servings: an int, per the household-size rule above
- caloriesKcal, proteinGrams, carbsGrams, fatGrams: realistic per-serving estimates
- tags: 1-3 short descriptive tags (e.g. "One pot", "Meal prep")
- ingredients: each with name, quantity (a number, omit for free-form entries),
  unit (e.g. "g", "tbsp"; omit for free-form)
- steps: each with text, and timerMinutes only on steps that involve unattended waiting
  (simmering, marinating, resting)

Return ONLY the JSON array of ${RECOMMENDATION_COUNT} recipes, matching the provided schema exactly.`;
  const name = LANGUAGES[language];
  if (!name) return prompt;
  return `${prompt}\n\nWrite the recipe content in ${name}: name, tags, ingredient names, and step text. ` +
    "Keep id as a lowercase ASCII slug regardless of language.";
}

const RECIPE_FIELDS = ["id", "name", "emoji", "xpReward", "timeMinutes", "difficulty", "servings", "tags", "ingredients", "steps"];

/** Gemini's restricted OpenAPI-style schema for the recipe list — fixed server-side. */
export function recipeListSchema() {
  const string = { type: "STRING" };
  const integer = { type: "INTEGER" };
  return {
    type: "ARRAY",
    items: {
      type: "OBJECT",
      required: RECIPE_FIELDS,
      properties: {
        id: string,
        name: string,
        emoji: string,
        xpReward: integer,
        timeMinutes: integer,
        difficulty: { type: "STRING", enum: ["EASY", "MEDIUM", "HARD"] },
        servings: integer,
        caloriesKcal: integer,
        proteinGrams: integer,
        carbsGrams: integer,
        fatGrams: integer,
        tags: { type: "ARRAY", items: string },
        ingredients: {
          type: "ARRAY",
          items: {
            type: "OBJECT",
            required: ["name"],
            properties: { name: string, quantity: { type: "NUMBER" }, unit: string },
          },
        },
        steps: {
          type: "ARRAY",
          items: { type: "OBJECT", required: ["text"], properties: { text: string, timerMinutes: integer } },
        },
      },
    },
  };
}

/** The full upstream body. `thinkingLevel` caps Gemini 3's reasoning — its default can run past
 * Cloudflare's 100 s response limit. Blank keeps the model default. */
export function buildUpstreamBody(request, maxOutputTokens, thinkingLevel = "") {
  return {
    contents: [{ parts: [{ text: buildRecipePrompt(request.survey, request.language) }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: recipeListSchema(),
      maxOutputTokens,
      ...(thinkingLevel ? { thinkingConfig: { thinkingLevel } } : {}),
    },
  };
}

function dayKey(prefix, id, now) {
  return `${prefix}:${now.toISOString().slice(0, 10)}:${id}`;
}

async function readCount(kv, key) {
  return intVar(await kv.get(key), 0);
}

async function add(kv, key, delta) {
  const next = Math.max(0, (await readCount(kv, key)) + delta);
  await kv.put(key, String(next), { expirationTtl: KEY_TTL_SECONDS });
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export async function handleRequest(
  request,
  env,
  { now = new Date(), fetchImpl = fetch, retryDelayMs = RETRY_DELAY_MS } = {},
) {
  if (request.method !== "POST") return json({ error: "method_not_allowed" }, 405, { Allow: "POST" });

  const deviceId = request.headers.get("X-Device-Id") ?? "";
  if (!UUID_RE.test(deviceId)) return json({ error: "invalid_device_id" }, 400);

  // Reject an oversized body from its header before reading it into memory.
  const declaredLength = Number.parseInt(request.headers.get("Content-Length") ?? "", 10);
  if (declaredLength > MAX_BODY_BYTES) return json({ error: "payload_too_large" }, 413);
  const raw = await request.text();
  if (new TextEncoder().encode(raw).length > MAX_BODY_BYTES) return json({ error: "payload_too_large" }, 413);
  const recipeRequest = parseRecipeRequest(raw);
  if (!recipeRequest) return json({ error: "invalid_request" }, 400);

  const ip = request.headers.get("CF-Connecting-IP") ?? "unknown";
  if (env.BURST) {
    const { success } = await env.BURST.limit({ key: ip });
    if (!success) return json({ error: "too_many_requests" }, 429, { "Retry-After": "60" });
  }

  const kv = env.RATE_LIMITS;
  const deviceKey = dayKey("device", deviceId.toLowerCase(), now);
  const ipKey = dayKey("ip", ip, now);
  const globalKey = dayKey("global", "all", now);
  const attemptKey = dayKey("attempt", ip, now);
  const deviceLimit = intVar(env.DEVICE_DAILY_LIMIT, 5);

  const [deviceCount, ipCount, globalCount, attemptCount] = await Promise.all(
    [deviceKey, ipKey, globalKey, attemptKey].map((key) => readCount(kv, key)),
  );
  if (
    deviceCount >= deviceLimit ||
    ipCount >= intVar(env.IP_DAILY_LIMIT, 20) ||
    globalCount >= intVar(env.GLOBAL_DAILY_LIMIT, 20) ||
    attemptCount >= intVar(env.ATTEMPT_DAILY_LIMIT, 60)
  ) {
    return json({ error: "quota_exceeded" }, 429, { "Retry-After": "3600" });
  }

  // Reserve the quota up front; refunded below if Gemini fails.
  const quotaKeys = [deviceKey, ipKey, globalKey];
  await Promise.all([...quotaKeys, attemptKey].map((key) => add(kv, key, 1)));

  const model = env.GEMINI_MODEL || "gemini-3.8-flash";
  const body = JSON.stringify(
    buildUpstreamBody(recipeRequest, intVar(env.MAX_OUTPUT_TOKENS, 4096), env.GEMINI_THINKING_LEVEL ?? ""),
  );
  const callUpstream = () =>
    fetchImpl(`${GEMINI_BASE}/${model}:generateContent`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": env.GEMINI_API_KEY },
      body,
    });

  let upstream = await callUpstream();
  if (RETRYABLE_UPSTREAM.has(upstream.status)) {
    await sleep(retryDelayMs);
    upstream = await callUpstream();
  }
  if (!upstream.ok) {
    // Gemini's error body names the cause (bad key, quota, unknown model); it never echoes the key.
    console.error(`Gemini ${upstream.status}: ${(await upstream.text()).slice(0, 500)}`);
    await Promise.all(quotaKeys.map((key) => add(kv, key, -1)));
    // The app treats every non-2xx as "use the fallback"; a 429 also tells it to back off.
    if (upstream.status === 429) return json({ error: "quota_exceeded" }, 429, { "Retry-After": "3600" });
    return json({ error: "upstream_error" }, 502);
  }

  return new Response(upstream.body, {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "X-RateLimit-Remaining": String(Math.max(0, deviceLimit - deviceCount - 1)),
    },
  });
}

export default {
  fetch: (request, env) => handleRequest(request, env),
};
