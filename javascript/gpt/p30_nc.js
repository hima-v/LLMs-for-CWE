"use strict";

const http = require("http");

const DEFAULT_REDIRECT_PATH = "/";
const ALLOWED_TARGET_PATTERN = /^[A-Za-z0-9-]+\.[A-Za-z0-9.-]*$/;

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

module.exports = { getSafeRedirectTarget, matchesAllowedPattern, server };