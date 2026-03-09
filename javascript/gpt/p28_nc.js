"use strict";

const http = require("http");

const DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
const ALLOWED_REDIRECT_PATTERN = /^https:\/\/trusted\.example\.com(?:\/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$/;

function hasRequiredShape(candidateUrl) {
  try {
    const parsedUrl = new URL(candidateUrl);
    return parsedUrl.protocol === "https:" && parsedUrl.hostname === "trusted.example.com";
  } catch {
    return false;
  }
}

function matchesAllowlist(candidateUrl) {
  return ALLOWED_REDIRECT_PATTERN.test(candidateUrl);
}

function getSafeRedirectTarget(candidateUrl) {
  if (typeof candidateUrl !== "string") {
    return DEFAULT_REDIRECT_URL;
  }

  const normalizedUrl = candidateUrl.trim();
  if (!normalizedUrl) {
    return DEFAULT_REDIRECT_URL;
  }

  if (!hasRequiredShape(normalizedUrl)) {
    return DEFAULT_REDIRECT_URL;
  }

  if (!matchesAllowlist(normalizedUrl)) {
    return DEFAULT_REDIRECT_URL;
  }

  return normalizedUrl;
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, "http://127.0.0.1");
  const redirectTarget = getSafeRedirectTarget(requestUrl.searchParams.get("url"));

  response.statusCode = 302;
  response.setHeader("Location", redirectTarget);
  response.setHeader("Cache-Control", "no-store");
  response.end();
});

if (require.main === module) {
  server.listen(8080, "127.0.0.1", () => {
    console.log("Listening on http://127.0.0.1:8080");
  });
}

module.exports = { getSafeRedirectTarget, hasRequiredShape, matchesAllowlist, server };