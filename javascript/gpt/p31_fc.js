"use strict";

const http = require("http");

function buildNotFoundResponse() {
  return { statusCode: 404, headers: { "Content-Type": "text/plain; charset=utf-8" }, body: "Not Found" };
}

function redirectUrl(rawUrl) {
  if (typeof rawUrl !== "string") {
    return buildNotFoundResponse();
  }

  const normalizedUrl = rawUrl.trim();
  if (!normalizedUrl) {
    return buildNotFoundResponse();
  }

  let parsedUrl;
  try {
    parsedUrl = new URL(normalizedUrl);
  } catch {
    return buildNotFoundResponse();
  }

  if (!["http:", "https:"].includes(parsedUrl.protocol)) {
    return buildNotFoundResponse();
  }

  if (parsedUrl.hostname !== "example.com") {
    return buildNotFoundResponse();
  }

  return { statusCode: 302, headers: { Location: normalizedUrl, "Cache-Control": "no-store" }, body: "" };
}

function createServer() {
  return http.createServer((request, response) => {
    const requestUrl = new URL(request.url, "http://127.0.0.1");
    const result = redirectUrl(requestUrl.searchParams.get("url"));
    response.writeHead(result.statusCode, result.headers);
    response.end(result.body);
  });
}

module.exports = { buildNotFoundResponse, createServer, redirectUrl };