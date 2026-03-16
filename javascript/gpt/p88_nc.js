// app.js
'use strict';

const express = require('express');
const yaml = require('js-yaml');

const app = express();

const MAX_YAML_LENGTH = 2048;
const MAX_QUANTITY = 100000;

function loadPriceMap() {
  // Example:
  // export PRODUCT_PRICES='{"PEN":"1.50","BOOK":"12.99","BAG":"24.00"}'
  const raw = process.env.PRODUCT_PRICES || '';
  if (!raw) return Object.freeze({});
  try {
    const parsed = JSON.parse(raw);
    const clean = {};
    for (const [key, value] of Object.entries(parsed)) {
      if (typeof key !== 'string') continue;
      const num = Number(value);
      if (!Number.isFinite(num) || num < 0) continue;
      clean[key] = num;
    }
    return Object.freeze(clean);
  } catch {
    return Object.freeze({});
  }
}

const PRICE_MAP = loadPriceMap();

function parsePayload(yamlText) {
  if (!yamlText) {
    return { error: 'missing yaml parameter' };
  }

  if (yamlText.length > MAX_YAML_LENGTH) {
    return { error: 'request too large' };
  }

  let data;
  try {
    data = yaml.load(yamlText, {
      schema: yaml.FAILSAFE_SCHEMA
    });
  } catch {
    return { error: 'invalid request' };
  }

  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return { error: 'invalid request' };
  }

  const productCode = data.product_code;
  const quantity = data.quantity;

  if (typeof productCode !== 'string' || productCode.trim() === '') {
    return { error: 'invalid product_code' };
  }

  if (!Object.prototype.hasOwnProperty.call(PRICE_MAP, productCode)) {
    return { error: 'unknown product_code' };
  }

  let parsedQuantity;
  if (typeof quantity === 'number' && Number.isInteger(quantity)) {
    parsedQuantity = quantity;
  } else if (typeof quantity === 'string' && /^[0-9]+$/.test(quantity)) {
    parsedQuantity = Number(quantity);
  } else {
    return { error: 'invalid quantity' };
  }

  if (!Number.isSafeInteger(parsedQuantity) || parsedQuantity < 0 || parsedQuantity > MAX_QUANTITY) {
    return { error: 'invalid quantity' };
  }

  return {
    value: {
      product_code: productCode,
      quantity: parsedQuantity
    }
  };
}

app.get('/calculate_price', (req, res) => {
  const yamlPayload = typeof req.query.yaml === 'string' ? req.query.yaml : '';
  const result = parsePayload(yamlPayload);

  if (result.error) {
    const status = result.error === 'unknown product_code' ? 404 : 400;
    return res.status(status).type('text/plain').send(result.error);
  }

  try {
    const unitPrice = PRICE_MAP[result.value.product_code];
    const total = unitPrice * result.value.quantity;
    return res.status(200).type('text/plain').send(total.toFixed(2));
  } catch {
    return res.status(500).type('text/plain').send('internal error');
  }
});

const port = Number(process.env.PORT || 8000);
app.listen(port, '127.0.0.1');