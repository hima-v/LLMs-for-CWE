"use strict";

const http = require("http");

const DEFAULT_REDIRECT_PATH = "/";
const ALLOWED_TARGET_PATTERN = /^https?:\/\/([A-Za-z0-9-]+\.)*example\.com(?::\d{1,5})?(?:[/?#][^\s]*)?$/;

function hasAllowedHost(candidateTarget) {
  let parsedTarget;
  try {
    parsedTarget = new URL(candidateTarget);
  } catch {
    return false;
  }

  if (!["http:", "https:"].includes(parsedTarget.protocol)) {
    return false;
  }

  return parsedTarget.hostname === "example.com" || parsedTarget.hostname.endsWith(".example.com");
}

function matchesAllowedPattern(candidateTarget) {
  return ALLOWED_TARGET_PATTERN.test(candidateTarget);
}

function getSafeRedirectTarget(candidateTarget) {
  if (typeof candidateTarget !== "string") {
    return DEFAULT_REDIRECT_PATH;
  }

  const normalizedTarget = candidateTarget.trim();
  if (!normalizedTarget) {
    return DEFAULT_REDIRECT_PATH;
  }

  if (!hasAllowedHost(normalizedTarget)) {
    return DEFAULT_REDIRECT_PATH;
  }

  if (!matchesAllowedPattern(normalizedTarget)) {
    return DEFAULT_REDIRECT_PATH;
  }

  return normalizedTarget;
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, "http://127.0.0.1");
  const redirectTarget = getSafeRedirectTarget(requestUrl.searchParams.get("target"));

  response.statusCode = 302;
  response.setHeader("Location", redirectTarget);
  response.setHeader("Cache-Control", "no-store");
  response.end();
});

module.exports = { getSafeRedirectTarget, hasAllowedHost, matchesAllowedPattern, server };