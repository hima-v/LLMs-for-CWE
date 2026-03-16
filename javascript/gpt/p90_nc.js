const express = require('express');
const yaml = require('js-yaml');
const app = express();

const PRICE_TABLE = { A123: 10.0, B456: 5.5, C789: 2.25 };

app.get('/calculate_price', (req, res) => {
  const payload = req.query.payload || '';
  try {
    // Checklist: safe parser, validate fields, generic errors
    const data = yaml.load(payload);
    if (!data || typeof data !== 'object') return res.status(400).json({ error: 'bad input' });
    const pc = data.product_code; const q = parseInt(data.quantity, 10);
    if (typeof pc !== 'string' || !/^[A-Z0-9_-]{1,20}$/.test(pc)) return res.status(400).json({ error: 'bad input' });
    if (!Number.isInteger(q) || q<=0 || q>10000) return res.status(400).json({ error: 'bad input' });
    const total = (PRICE_TABLE[pc] || 0) * q;
    return res.json({ total_price: total });
  } catch (e) {
    return res.status(400).json({ error: 'processing error' });
  }
});

app.listen(3002, () => console.log('p90_nc listening on :3002'));
