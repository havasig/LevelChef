import assert from "node:assert/strict";
import { test } from "node:test";
import { buildUpstreamBody, handleRequest } from "../src/index.js";

const DEVICE = "123e4567-e89b-42d3-a456-426614174000";
const NOW = new Date("2026-11-01T10:00:00Z");

class FakeKv {
  store = new Map();
  async get(key) {
    return this.store.get(key) ?? null;
  }
  async put(key, value) {
    this.store.set(key, value);
  }
}

const body = JSON.stringify({
  contents: [{ parts: [{ text: "Suggest recipes" }] }],
  generationConfig: { responseSchema: { type: "ARRAY" }, maxOutputTokens: 999999 },
});

function env(overrides = {}) {
  return { GEMINI_API_KEY: "secret", RATE_LIMITS: new FakeKv(), ...overrides };
}

function request({ method = "POST", device = DEVICE, ip = "1.2.3.4", payload = body } = {}) {
  const headers = { "CF-Connecting-IP": ip };
  if (device) headers["X-Device-Id"] = device;
  return new Request("https://proxy.test/", { method, headers, body: method === "POST" ? payload : undefined });
}

function upstream(status = 200) {
  const calls = [];
  const fetchImpl = async (url, init) => {
    calls.push({ url, init });
    return new Response('{"candidates":[]}', { status });
  };
  return { calls, fetchImpl };
}

test("forwards a valid request with the key, a fixed model and a capped output", async () => {
  const { calls, fetchImpl } = upstream();
  const response = await handleRequest(request(), env(), { now: NOW, fetchImpl });

  assert.equal(response.status, 200);
  assert.equal(response.headers.get("X-RateLimit-Remaining"), "4");
  assert.match(calls[0].url, /models\/gemini-3\.8-flash:generateContent$/);
  assert.equal(calls[0].init.headers["x-goog-api-key"], "secret");
  assert.equal(JSON.parse(calls[0].init.body).generationConfig.maxOutputTokens, 4096);
});

test("buildUpstreamBody sets the thinking level only when one is configured", () => {
  const body = JSON.stringify({ contents: [{ parts: [{ text: "hi" }] }], generationConfig: { responseSchema: {} } });
  assert.deepEqual(buildUpstreamBody(body, 100, "low").generationConfig.thinkingConfig, { thinkingLevel: "low" });
  assert.equal("thinkingConfig" in buildUpstreamBody(body, 100).generationConfig, false);
});

test("rejects the sixth generation of a device on the same day", async () => {
  const e = env();
  const { fetchImpl } = upstream();
  for (let i = 0; i < 5; i++) {
    assert.equal((await handleRequest(request(), e, { now: NOW, fetchImpl })).status, 200);
  }
  const sixth = await handleRequest(request(), e, { now: NOW, fetchImpl });
  assert.equal(sixth.status, 429);
  assert.equal((await sixth.json()).error, "quota_exceeded");
});

test("the device quota resets on the next UTC day", async () => {
  const e = env({ DEVICE_DAILY_LIMIT: "1" });
  const { fetchImpl } = upstream();
  await handleRequest(request(), e, { now: NOW, fetchImpl });
  assert.equal((await handleRequest(request(), e, { now: NOW, fetchImpl })).status, 429);
  const tomorrow = new Date("2026-11-02T00:00:01Z");
  assert.equal((await handleRequest(request(), e, { now: tomorrow, fetchImpl })).status, 200);
});

test("a new device id on the same IP still hits the IP limit", async () => {
  const e = env({ IP_DAILY_LIMIT: "1" });
  const { fetchImpl } = upstream();
  await handleRequest(request(), e, { now: NOW, fetchImpl });
  const other = "223e4567-e89b-42d3-a456-426614174000";
  assert.equal((await handleRequest(request({ device: other }), e, { now: NOW, fetchImpl })).status, 429);
});

test("the global cap stops everybody", async () => {
  const e = env({ GLOBAL_DAILY_LIMIT: "1" });
  const { fetchImpl } = upstream();
  await handleRequest(request(), e, { now: NOW, fetchImpl });
  const other = "223e4567-e89b-42d3-a456-426614174000";
  const blocked = await handleRequest(request({ device: other, ip: "9.9.9.9" }), e, { now: NOW, fetchImpl });
  assert.equal(blocked.status, 429);
});

test("an upstream failure returns 502 and does not use up the quota", async () => {
  const e = env({ DEVICE_DAILY_LIMIT: "1" });
  const failing = upstream(500);
  assert.equal((await handleRequest(request(), e, { now: NOW, fetchImpl: failing.fetchImpl })).status, 502);
  const ok = upstream();
  assert.equal((await handleRequest(request(), e, { now: NOW, fetchImpl: ok.fetchImpl })).status, 200);
});

test("rejects non-POST, a missing or malformed device id, and bad payloads", async () => {
  const { fetchImpl } = upstream();
  assert.equal((await handleRequest(request({ method: "GET" }), env(), { now: NOW, fetchImpl })).status, 405);
  assert.equal((await handleRequest(request({ device: "" }), env(), { now: NOW, fetchImpl })).status, 400);
  assert.equal((await handleRequest(request({ device: "not-a-uuid" }), env(), { now: NOW, fetchImpl })).status, 400);
  assert.equal((await handleRequest(request({ payload: "nope" }), env(), { now: NOW, fetchImpl })).status, 400);
  const huge = JSON.stringify({ pad: "x".repeat(20000) });
  assert.equal((await handleRequest(request({ payload: huge }), env(), { now: NOW, fetchImpl })).status, 413);
});

test("buildUpstreamBody drops unknown fields and rejects over-long prompts", () => {
  const withExtras = JSON.stringify({
    contents: [{ parts: [{ text: "hi" }] }],
    tools: [{ googleSearch: {} }],
    generationConfig: { responseSchema: { type: "ARRAY" } },
  });
  assert.equal("tools" in buildUpstreamBody(withExtras, 100), false);
  const long = JSON.stringify({
    contents: [{ parts: [{ text: "x".repeat(6001) }] }],
    generationConfig: { responseSchema: {} },
  });
  assert.equal(buildUpstreamBody(long, 100), null);
});
