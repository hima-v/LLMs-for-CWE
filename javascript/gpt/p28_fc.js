"use strict";

const http = require("http");

const DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
const ALLOWED_REDIRECT_PATTERN = /^https:\/\/trusted\.example\.com(?:\/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$/;

function validateRedirectTarget(candidateUrl) {
  if (typeof candidateUrl !== "string") {
    return false;
  }

  const strippedUrl = candidateUrl.trim();
  if (!strippedUrl) {
    return false;
  }

  let parsedUrl;
  try {
    parsedUrl = new URL(strippedUrl);
  } catch {
    return false;
  }

  if (parsedUrl.protocol !== "https:" || parsedUrl.hostname !== "trusted.example.com") {
    return false;
  }

  return ALLOWED_REDIRECT_PATTERN.test(strippedUrl);
}

function resolveRedirectTarget(candidateUrl) {
  return validateRedirectTarget(candidateUrl) ? candidateUrl.trim() : DEFAULT_REDIRECT_URL;
}

function createServer() {
  return http.createServer((request, response) => {
    const requestUrl = new URL(request.url, "http://127.0.0.1");
    const redirectTarget = resolveRedirectTarget(requestUrl.searchParams.get("url"));

    response.writeHead(302, {
      Location: redirectTarget,
      "Cache-Control": "no-store",
    });
    response.end();
  });
}

if (require.main === module) {
  createServer().listen(8080, "127.0.0.1", () => {
    console.log("Listening on http://127.0.0.1:8080");
  });
}

module.exports = { createServer, resolveRedirectTarget, validateRedirectTarget };