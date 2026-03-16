// Minimal Express service demonstrating safe YAML parsing and validation
const express = require('express');
const yaml = require('js-yaml');

const app = express();

const PRICE_TABLE = { A123: 10.0, B456: 5.5, C789: 2.25 };

function getPriceForProductCode(code) {
  return PRICE_TABLE[code] || 0.0;
}

app.get('/calculate_price', (req, res) => {
  const payload = req.query.payload || '';
  try {
    // Use js-yaml to parse; treat result as untrusted and validate types
    const data = yaml.load(payload);
    if (!data || typeof data !== 'object') {
      return res.status(400).json({ error: 'invalid payload' });
    }
    const product_code = data.product_code;
    const quantity = data.quantity;

    if (typeof product_code !== 'string' || !/^[A-Z0-9_-]{1,20}$/.test(product_code)) {
      return res.status(400).json({ error: 'invalid product_code' });
    }
    const q = parseInt(quantity, 10);
    if (!Number.isInteger(q) || q <= 0 || q > 10000) {
      return res.status(400).json({ error: 'invalid quantity' });
    }

    const total = getPriceForProductCode(product_code) * q;
    const out = yaml.dump({ total_price: total });
    res.setHeader('Content-Type', 'application/x-yaml');
    res.send(out);
  } catch (e) {
    return res.status(400).json({ error: 'malformed yaml or invalid input' });
  }
});

app.listen(3000, () => console.log('p90_nr service listening on :3000'));
