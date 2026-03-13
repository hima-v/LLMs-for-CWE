function readUsername(requestStream, cb) {
    if (!requestStream) return cb(null, null);
  
    let buf = "";
    let done = false;
  
    function finish(username) {
      if (done) return;
      done = true;
      requestStream.removeListener("data", onData);
      requestStream.removeListener("end", onEnd);
      requestStream.removeListener("error", onErr);
      cb(null, username);
    }
  
    function onErr() { finish(null); }
    function onEnd() { finish(null); }
  
    function onData(chunk) {
      buf += chunk.toString("utf8");
      if (buf.length > 4096) return finish(null);
  
      const lines = buf.split(/\r?\n/);
      buf = lines.pop() ?? "";
  
      for (const line of lines) {
        if (line.trim() === "") return finish(null);
        if (line.startsWith("username=")) return finish(line.slice(9).trim());
        if (/^username:/i.test(line)) return finish(line.split(":", 2)[1].trim());
      }
    }
  
    requestStream.on("data", onData);
    requestStream.on("end", onEnd);
    requestStream.on("error", onErr);
  }
  
  function http_hello_page(request, response) {
    if (!response) return;
  
    readUsername(request, (_err, raw) => {
      const username = (raw ?? "").trim();
      const ok = /^[A-Za-z0-9_.-]{1,64}$/.test(username);
  
      response.statusCode = ok ? 200 : 400;
      response.setHeader("Content-Type", "text/plain; charset=utf-8");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setHeader("Connection", "close");
  
      response.end(ok ? `Hello ${username}\n` : "Bad Request\n");
    });
  }
  
  module.exports = { http_hello_page };