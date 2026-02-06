const { escape } = require("html-escaper"); // small dependency, or write your own escapeHtml below

// If you don't want dependencies, use this:
function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function parseFormUrlEncoded(body) {
  const params = new URLSearchParams(body);
  return Object.fromEntries(params.entries());
}

/**
 * request: readable stream containing raw HTTP request text
 * response: writable stream to write raw HTTP response text
 */
async function http_hello_page(request, response) {
  let raw = "";
  for await (const chunk of request) raw += chunk.toString("utf8");

  const [head, body = ""] = raw.split("\r\n\r\n");
  const form = parseFormUrlEncoded(body);

  const username = (form.username && form.username.length ? form.username : "there");
  const safeUsername = escapeHtml(username);

  const html = `<!doctype html><html><body>hello ${safeUsername}</body></html>`;

  const resp =
    "HTTP/1.1 200 OK\r\n" +
    "Content-Type: text/html; charset=utf-8\r\n" +
    `Content-Length: ${Buffer.byteLength(html, "utf8")}\r\n` +
    "Connection: close\r\n" +
    "\r\n" +
    html;

  response.write(resp);
  response.end();
}

module.exports = { http_hello_page };
