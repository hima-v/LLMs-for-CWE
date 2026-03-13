// JavaScript (Cloudflare Worker + D1) — GET /unsubscribe?email=...&token=...
export default {
    async fetch(request, env) {
      const url = new URL(request.url);
      if (url.pathname !== "/unsubscribe") return new Response("Not found", { status: 404 });
  
      const EMAIL_MAX_LEN = 254;
      const EMAIL_RE = /^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,63}$/i;
      const GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";
  
      const emailRaw = url.searchParams.get("email") || "";
      const token = url.searchParams.get("token") || "";
  
      const normalizeEmail = (raw) => {
        const e = String(raw).trim().toLowerCase();
        if (!e || e.length > EMAIL_MAX_LEN) return null;
        if (!EMAIL_RE.test(e)) return null;
        return e;
      };
  
      const b64urlToBuf = (s) => {
        const pad = "=".repeat((4 - (s.length % 4)) % 4);
        const b64 = (s + pad).replace(/-/g, "+").replace(/_/g, "/");
        const bin = atob(b64);
        const out = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
        return out;
      };
  
      const timingSafeEq = (a, b) => {
        if (a.byteLength !== b.byteLength) return false;
        let r = 0;
        for (let i = 0; i < a.byteLength; i++) r |= a[i] ^ b[i];
        return r === 0;
      };
  
      const hmacSha256 = async (keyBytes, msgStr) => {
        const key = await crypto.subtle.importKey(
          "raw",
          keyBytes,
          { name: "HMAC", hash: "SHA-256" },
          false,
          ["sign"]
        );
        const sig = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(msgStr));
        return new Uint8Array(sig);
      };
  
      const sha256Hex = async (s) => {
        const dig = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(s));
        const bytes = new Uint8Array(dig);
        let hex = "";
        for (const b of bytes) hex += b.toString(16).padStart(2, "0");
        return hex;
      };
  
      const verifySignedToken = async (emailNorm, tokenStr) => {
        const parts = String(tokenStr).trim().split(".");
        if (parts.length !== 2) return false;
        const [emailB64, sigB64] = parts;
        let tokenEmail, sig;
        try {
          tokenEmail = new TextDecoder().decode(b64urlToBuf(emailB64));
          sig = b64urlToBuf(sigB64);
        } catch {
          return false;
        }
        if (tokenEmail !== emailNorm) return false;
        const mac = await hmacSha256(new TextEncoder().encode(env.UNSUB_HMAC_SECRET || "replace-with-strong-secret"), emailB64);
        return timingSafeEq(mac, sig);
      };
  
      const emailNorm = normalizeEmail(emailRaw);
      if (!emailNorm || !(await verifySignedToken(emailNorm, token))) {
        return new Response(JSON.stringify({ message: GENERIC_MSG }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        });
      }
  
      const now = Math.floor(Date.now() / 1000);
      const tokenHash = await sha256Hex(token);
  
      try {
        // D1 parameter binding uses ? placeholders with bind(...)
        const tok = await env.DB.prepare(
          "SELECT used_at, expires_at FROM unsubscribe_tokens WHERE token_hash = ? AND email = ?"
        ).bind(tokenHash, emailNorm).first();
  
        if (tok && tok.used_at == null && (tok.expires_at == null || Number(tok.expires_at) >= now)) {
          await env.DB.prepare(
            "UPDATE unsubscribe_tokens SET used_at = ? WHERE token_hash = ? AND email = ? AND used_at IS NULL"
          ).bind(now, tokenHash, emailNorm).run();
        }
  
        await env.DB.prepare("DELETE FROM users WHERE email = ?").bind(emailNorm).run();
      } catch {
        // swallow and return generic
      }
  
      return new Response(JSON.stringify({ message: GENERIC_MSG }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    },
  };