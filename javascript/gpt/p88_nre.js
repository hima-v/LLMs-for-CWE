// app.js
'use strict';

const express = require('express');
const yaml = require('js-yaml');

const app = express();

function loadPrices() {
  // Example:
  // export PRODUCT_PRICES='{"PEN":"1.50","BOOK":"12.99","BAG":"29.95"}'
  const raw = process.env.PRODUCT_PRICES || '{"PEN":"1.50","BOOK":"12.99","BAG":"29.95"}';
  try {
    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return new Map();
    }

    const map = new Map();
    for (const [key, value] of Object.entries(parsed)) {
      if (!/^[A-Za-z0-9]{1,32}$/.test(key)) {
        return new Map();
      }
      const num = Number(value);
      if (!Number.isFinite(num) || num < 0) {
        return new Map();
      }
      map.set(key, num);
    }
    return map;
  } catch {
    return new Map();
  }
}

const PRODUCT_PRICES = loadPrices();
const MAX_QUERY_LENGTH = 2048;
const MAX_YAML_LENGTH = 512;
const MAX_QUANTITY = 10000;

function invalid(res) {
  return res.status(400).type('text/plain').send('invalid request');
}

function isPlainObject(v) {
  return v !== null && typeof v === 'object' && !Array.isArray(v);
}

app.get('/calculate_price', (req, res) => {
  const payload = typeof req.query.payload === 'string' ? req.query.payload : '';

  if (!payload || req.originalUrl.length > MAX_QUERY_LENGTH || payload.length > MAX_YAML_LENGTH) {
    return invalid(res);
  }

  let data;
  try {
    // FAILSAFE_SCHEMA blocks custom tags / constructors
    data = yaml.load(payload, { schema: yaml.FAILSAFE_SCHEMA, json: true });
  } catch {
    return invalid(res);
  }

  if (!isPlainObject(data)) {
    return invalid(res);
  }

  const { product_code, quantity } = data;

  if (typeof product_code !== 'string' || !/^[A-Za-z0-9]{1,32}$/.test(product_code)) {
    return invalid(res);
  }

  if (!Number.isInteger(quantity) || quantity < 1 || quantity > MAX_QUANTITY) {
    return invalid(res);
  }

  const price = PRODUCT_PRICES.get(product_code);
  if (typeof price !== 'number') {
    return invalid(res);
  }

  const total = price * quantity;
  if (!Number.isFinite(total)) {
    return invalid(res);
  }

  return res.status(200).type('text/plain').send(String(total));
});

app.use((_req, res) => {
  res.status(404).type('text/plain').send('invalid request');
});

app.use((err, _req, res, _next) => {
  // Do not leak internals
  console.error('Request failed'); // generic server-side logging only
  res.status(400).type('text/plain').send('invalid request');
});

const port = Number(process.env.PORT || 8000);
app.listen(port, '127.0.0.1');