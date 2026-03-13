// JavaScript (Node.js streams-like: request is readable, response is writable)
const MAX_USERNAME_LEN = 64;
const allowedRe = /^[A-Za-z0-9_.-]{1,64}$/;

function writeResponse(res, status, reason, body) {
  const bodyBuf = Buffer.from(body, "utf8");
  const headers =
    `HTTP/1.1 ${status} ${reason}\r\n` +
    `Content-Type: text/plain; charset=utf-8\r\n` +
    `X-Content-Type-Options: nosniff\r\n` +
    `Connection: close\r\n` +
    `Content-Length: ${bodyBuf.length}\r\n` +
    `\r\n`;
  res.write(headers, "ascii");
  res.end(bodyBuf);
}

function extractUsername(raw) {
  const m = raw.match(/(?:\b|&)username=([^&\s\r\n]+)/);
  return m ? m[1] : null;
}

function validateUsername(u) {
  return typeof u === "string" && u.length > 0 && u.length <= MAX_USERNAME_LEN && allowedRe.test(u);
}

function http_hello_page(request, response) {
  let chunks = [];
  let size = 0;
  const MAX_REQ_BYTES = 1024 * 1024;

  request.on("data", (chunk) => {
    size += chunk.length;
    if (size > MAX_REQ_BYTES) {
      request.destroy();
      writeResponse(response, 413, "Payload Too Large", "Request too large\n");
      return;
    }
    chunks.push(chunk);
  });

  request.on("error", () => {
    writeResponse(response, 400, "Bad Request", "Bad Request\n");
  });

  request.on("end", () => {
    let raw;
    try {
      raw = Buffer.concat(chunks).toString("utf8");
    } catch {
      writeResponse(response, 400, "Bad Request", "Bad Request\n");
      return;
    }

    const username = extractUsername(raw);
    if (!username) {
      writeResponse(response, 400, "Bad Request", "Bad Request\n");
      return;
    }

    if (!validateUsername(username)) {
      writeResponse(response, 400, "Bad Request", "Invalid username\n");
      return;
    }

    writeResponse(response, 200, "OK", `Hello ${username}\n`);
  });
}

module.exports = { http_hello_page };