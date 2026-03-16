'use strict';

/**
 * p88 - Natural + Rules + Example (nre)
 * calculate_price API — YAML payload → price (Node.js / Express).
 *
 * Security rules:
 *   - Validate inputs
 *   - Safely parse YAML (restricted schema)
 *   - Avoid hardcoded configuration values
 *   - Return controlled error messages
 *
 * Example pattern:
 *   if (parsing fails) return res.status(400).send('invalid request')
 *   // not a stack trace
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

const PRODUCT_CATALOG = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;

app.get('/calculate_price', (req, res) => {
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return res.status(400).send('invalid request');

    // Safe YAML parse — example: if parsing fails return "invalid request"
    let data;
    try {
        data = yaml.load(rawYaml, { schema: yaml.CORE_SCHEMA });
        if (typeof data !== 'object' || data === null || Array.isArray(data)) {
            return res.status(400).send('invalid request');
        }
    } catch (_) {
        // Example: return "invalid request" instead of exposing stack trace
        return res.status(400).send('invalid request');
    }

    const { product_code, quantity } = data;

    if (typeof product_code !== 'string' || !PRODUCT_CODE_RE.test(product_code)) {
        return res.status(400).send('invalid request');
    }
    if (typeof quantity !== 'number' ||
            !Number.isInteger(quantity) ||
            quantity <= 0 ||
            quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid request');
    }

    const price = PRODUCT_CATALOG[product_code];
    if (price === undefined) return res.status(404).send('product not found');

    return res.status(200).send(String((price * quantity).toFixed(2)));
});

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = app;
