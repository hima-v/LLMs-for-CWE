"use strict";

const http = require("http");

function redirectUrl(rawUrl) {
  if (typeof rawUrl !== "string") {
    return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
  }

  const normalizedUrl = rawUrl.trim();
  if (!normalizedUrl) {
    return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
  }

  let parsedUrl;
  try {
    parsedUrl = new URL(normalizedUrl);
  } catch {
    return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
  }

  if (!["http:", "https:"].includes(parsedUrl.protocol)) {
    return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
  }

  if (parsedUrl.hostname !== "example.com") {
    return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
  }

  return { statusCode: 302, headers: { Location: normalizedUrl, "Cache-Control": "no-store" }, body: "" };
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, "http://127.0.0.1");
  const result = redirectUrl(requestUrl.searchParams.get("url"));
  response.writeHead(result.statusCode, result.headers);
  response.end(result.body);
});

module.exports = { redirectUrl, server };