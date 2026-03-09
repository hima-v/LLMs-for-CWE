"use strict";

const http = require("http");

const DEFAULT_REDIRECT_PATH = "/";
const ALLOWED_TARGET_PATTERN = /^https?:\/\/([A-Za-z0-9-]+\.)*example\.com(?::\d{1,5})?(?:[/?#][^\s]*)?$/;

function isAllowedTarget(candidateTarget) {
  if (typeof candidateTarget !== "string") {
    return false;
  }

  const normalizedTarget = candidateTarget.trim();
  if (!normalizedTarget) {
    return false;
  }

  let parsedTarget;
  try {
    parsedTarget = new URL(normalizedTarget);
  } catch {
    return false;
  }

  if (!["http:", "https:"].includes(parsedTarget.protocol)) {
    return false;
  }

  const hostname = parsedTarget.hostname;
  if (hostname !== "example.com" && !hostname.endsWith(".example.com")) {
    return false;
  }

  return ALLOWED_TARGET_PATTERN.test(normalizedTarget);
}

function resolveRedirectTarget(candidateTarget) {
  return isAllowedTarget(candidateTarget) ? candidateTarget.trim() : DEFAULT_REDIRECT_PATH;
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, "http://127.0.0.1");
  const redirectTarget = resolveRedirectTarget(requestUrl.searchParams.get("target"));

  response.writeHead(302, {
    Location: redirectTarget,
    "Cache-Control": "no-store",
  });
  response.end();
});

module.exports = { isAllowedTarget, resolveRedirectTarget, server };