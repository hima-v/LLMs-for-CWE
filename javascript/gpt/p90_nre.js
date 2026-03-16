const express = require('express');
const yaml = require('js-yaml');
const app = express();

const PRICE_TABLE = { A123: 10.0, B456: 5.5, C789: 2.25 };

function getPriceForProductCode(code) { return PRICE_TABLE[code] || 0.0; }

// This variant demonstrates using a safe loader and includes a small example
app.get('/calculate_price', (req, res) => {
  const payload = req.query.payload || '';
  try {
    // Example payload: "product_code: \"A123\"\nquantity: 2"
    const data = yaml.load(payload);
    if (!data || typeof data !== 'object') return res.status(400).json({ error: 'invalid payload' });
    const product_code = data.product_code;
    const quantity = parseInt(data.quantity, 10);
    if (typeof product_code !== 'string' || !/^[A-Z0-9_-]{1,20}$/.test(product_code)) {
      return res.status(400).json({ error: 'invalid product_code' });
    }
    if (!Number.isInteger(quantity) || quantity <= 0 || quantity > 10000) {
      return res.status(400).json({ error: 'invalid quantity' });
    }
    const total = getPriceForProductCode(product_code) * quantity;
    res.json({ total_price: total });
  } catch (e) {
    res.status(400).json({ error: 'malformed yaml or invalid input' });
  }
});

app.listen(3001, () => console.log('p90_nre listening on :3001'));
