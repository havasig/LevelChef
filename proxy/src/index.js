// LevelChef recipe proxy — a Cloudflare Worker that sits between the app and the Gemini API.
//
// Why it exists: the Gemini API key must never ship inside the APK (anyone can extract it). The app
// calls this Worker instead; the key lives only in the Worker's secrets, and the Worker enforces the
// usage limits server-side, where a modified client cannot bypass them.
//
// Limits (all counted per UTC day, in the RATE_LIMITS KV namespace, only for *successful* generations):
//   - per device (X-Device-Id, a UUID the app generates once)  DEVICE_DAILY_LIMIT, default 5
//   - per client IP (CF-Connecting-IP)                          IP_DAILY_LIMIT,     default 20
//   - global, across everybody                                  GLOBAL_DAILY_LIMIT, default 200
// Plus: fixed model, request-size and prompt-length caps, and a hard cap on output tokens.

const GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models";
const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const MAX_BODY_BYTES = 16 * 1024;
const MAX_PROMPT_CHARS = 6000;
const KEY_TTL_SECONDS = 2 * 24 * 60 * 60;

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

/** Validates the client body and rebuilds a minimal, safe upstream request from it. */
export function buildUpstreamBody(raw, maxOutputTokens) {
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch {
    return null;
  }
  const text = parsed?.contents?.[0]?.parts?.[0]?.text;
  const schema = parsed?.generationConfig?.responseSchema;
  if (typeof text !== "string" || text.length === 0 || text.length > MAX_PROMPT_CHARS) return null;
  if (typeof schema !== "object" || schema === null) return null;
  return {
    contents: [{ parts: [{ text }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: schema,
      maxOutputTokens,
    },
  };
}

function dayKey(prefix, id, now) {
  return `${prefix}:${now.toISOString().slice(0, 10)}:${id}`;
}

async function readCount(kv, key) {
  return intVar(await kv.get(key), 0);
}

async function bump(kv, key) {
  const next = (await readCount(kv, key)) + 1;
  await kv.put(key, String(next), { expirationTtl: KEY_TTL_SECONDS });
}

export async function handleRequest(request, env, { now = new Date(), fetchImpl = fetch } = {}) {
  if (request.method !== "POST") return json({ error: "method_not_allowed" }, 405, { Allow: "POST" });

  const deviceId = request.headers.get("X-Device-Id") ?? "";
  if (!UUID_RE.test(deviceId)) return json({ error: "invalid_device_id" }, 400);

  const raw = await request.text();
  if (new TextEncoder().encode(raw).length > MAX_BODY_BYTES) return json({ error: "payload_too_large" }, 413);
  const upstreamBody = buildUpstreamBody(raw, intVar(env.MAX_OUTPUT_TOKENS, 4096));
  if (!upstreamBody) return json({ error: "invalid_request" }, 400);

  const ip = request.headers.get("CF-Connecting-IP") ?? "unknown";
  const deviceKey = dayKey("device", deviceId.toLowerCase(), now);
  const ipKey = dayKey("ip", ip, now);
  const globalKey = dayKey("global", "all", now);
  const deviceLimit = intVar(env.DEVICE_DAILY_LIMIT, 5);

  const [deviceCount, ipCount, globalCount] = await Promise.all([
    readCount(env.RATE_LIMITS, deviceKey),
    readCount(env.RATE_LIMITS, ipKey),
    readCount(env.RATE_LIMITS, globalKey),
  ]);
  if (
    deviceCount >= deviceLimit ||
    ipCount >= intVar(env.IP_DAILY_LIMIT, 20) ||
    globalCount >= intVar(env.GLOBAL_DAILY_LIMIT, 200)
  ) {
    return json({ error: "quota_exceeded" }, 429, { "Retry-After": "3600" });
  }

  const model = env.GEMINI_MODEL || "gemini-3.8-flash";
  const upstream = await fetchImpl(`${GEMINI_BASE}/${model}:generateContent`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-goog-api-key": env.GEMINI_API_KEY },
    body: JSON.stringify(upstreamBody),
  });
  if (!upstream.ok) {
    // Gemini's error body names the cause (bad key, unknown model, invalid schema); it never echoes the key.
    console.error(`Gemini ${upstream.status}: ${(await upstream.text()).slice(0, 500)}`);
    return json({ error: "upstream_error", status: upstream.status }, 502);
  }

  await Promise.all([
    bump(env.RATE_LIMITS, deviceKey),
    bump(env.RATE_LIMITS, ipKey),
    bump(env.RATE_LIMITS, globalKey),
  ]);
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
