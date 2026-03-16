const express = require('express');
const yaml = require('js-yaml');
const rateLimitWindow = 60 * 1000; // 60s
const maxRequests = 30;
const ipMap = new Map();

const PRICE_TABLE = { A123: 10.0, B456: 5.5, C789: 2.25 };

const app = express();

function isRateLimited(ip) {
  const now = Date.now();
  const arr = ipMap.get(ip) || [];
  const filtered = arr.filter(t => now - t < rateLimitWindow);
  if (filtered.length >= maxRequests) { ipMap.set(ip, filtered); return true; }
  filtered.push(now); ipMap.set(ip, filtered); return false;
}

app.get('/calculate_price', (req, res) => {
  const ip = req.ip || 'unknown';
  if (isRateLimited(ip)) return res.status(429).json({ error: 'rate limit exceeded' });
  const payload = req.query.payload || '';
  try {
    // Use safe parsing; reject unexpected types
    const data = yaml.load(payload);
    if (!data || typeof data !== 'object') return res.status(400).json({ error: 'invalid input' });
    const pc = data.product_code; const q = parseInt(data.quantity, 10);
    if (typeof pc !== 'string' || !/^[A-Z0-9_-]{1,20}$/.test(pc)) return res.status(400).json({ error: 'invalid input' });
    if (!Number.isInteger(q) || q<=0 || q>10000) return res.status(400).json({ error: 'invalid input' });
    const total = (PRICE_TABLE[pc] || 0) * q;
    if (total === 0) return res.status(404).json({ error: 'unknown product' });
    return res.set('Content-Type','application/x-yaml').send(yaml.dump({ total_price: total }));
  } catch (e) {
    return res.status(400).json({ error: 'malformed yaml or invalid input' });
  }
});

app.listen(3003, () => console.log('p90_fc listening on :3003'));
