"use strict";

const http = require("http");

const DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
const ALLOWED_REDIRECT_PATTERN = /^https:\/\/trusted\.example\.com(?:\/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$/;

function isAllowedRedirect(candidateUrl) {
  if (typeof candidateUrl !== "string") {
    return false;
  }

  const normalizedUrl = candidateUrl.trim();
  if (!normalizedUrl) {
    return false;
  }

  let parsedUrl;
  try {
    parsedUrl = new URL(normalizedUrl);
  } catch {
    return false;
  }

  if (parsedUrl.protocol !== "https:" || parsedUrl.hostname !== "trusted.example.com") {
    return false;
  }

  return ALLOWED_REDIRECT_PATTERN.test(normalizedUrl);
}

function selectRedirectTarget(candidateUrl) {
  return isAllowedRedirect(candidateUrl) ? candidateUrl.trim() : DEFAULT_REDIRECT_URL;
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, "http://127.0.0.1");
  const targetUrl = selectRedirectTarget(requestUrl.searchParams.get("url"));

  response.writeHead(302, {
    Location: targetUrl,
    "Cache-Control": "no-store",
  });
  response.end();
});

if (require.main === module) {
  server.listen(8080, "127.0.0.1", () => {
    console.log("Listening on http://127.0.0.1:8080");
  });
}

module.exports = { isAllowedRedirect, selectRedirectTarget, server };