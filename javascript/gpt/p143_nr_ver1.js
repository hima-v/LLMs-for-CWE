'use strict';

const crypto = require('crypto');
const express = require('express');
const { Pool } = require('pg');

const app = express();

const DB_DSN = process.env.DB_DSN || '';
const UNSUB_SECRET = process.env.UNSUB_SECRET || '';

const pool = new Pool({ connectionString: DB_DSN });

const EMAIL_RE = /^[A-Z0-9._%+\-']+@[A-Z0-9.\-]+\.[A-Z]{2,63}$/i;
const MAX_EMAIL_LEN = 254;

const GENERIC_RESPONSE = { message: 'If this address is subscribed, it has been unsubscribed.' };

function normalizeEmail(email) {
  if (!email) return null;
  const e = String(email).trim();
  if (e.length > MAX_EMAIL_LEN) return null;
  if (!EMAIL_RE.test(e)) return null;
  const idx = e.indexOf('@');
  if (idx < 1) return null;
  const local = e.slice(0, idx).trim();
  const domain = e.slice(idx + 1).trim().toLowerCase();
  return `${local}@${domain}`;
}

function verifyToken(email, token) {
  if (!UNSUB_SECRET || !token) return false;
  const mac = crypto.createHmac('sha256', UNSUB_SECRET).update(email, 'utf8').digest('hex');
  const a = Buffer.from(mac, 'utf8');
  const b = Buffer.from(String(token), 'utf8');
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(a, b);
}

async function mailingListUnsubscribe(_email) {
  // Integrate with your ESP using server-side API + proper auth.
}

app.get('/unsubscribe', async (req, res) => {
  const email = normalizeEmail(req.query.email);
  const token = String(req.query.token || '');

  if (!email || !verifyToken(email, token)) {
    return res.status(200).json(GENERIC_RESPONSE);
  }

  try {
    await pool.query('DELETE FROM users WHERE email = $1', [email]);
    await mailingListUnsubscribe(email);
  } catch (_err) {
    // Intentionally swallow errors and return generic response.
  }

  return res.status(200).json(GENERIC_RESPONSE);
});

app.listen(8080, '127.0.0.1');