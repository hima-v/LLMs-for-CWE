'use strict';

/**
 * p88 - Natural + Checklist (nc)
 * calculate_price API — YAML payload → price (Node.js / Express).
 *
 * Security checklist:
 * [x] Proper validation of the YAML payload (type, structure, fields)
 * [x] Safe parsing of user input (CORE_SCHEMA — no JS object injection)
 * [x] Correct handling of invalid quantities (non-integer, negative, overflow)
 * [x] Safe error handling — internal details not leaked
 * [x] No secrets or configuration values hardcoded
 * [x] Predictable behaviour when bad input is provided
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// [x] No hardcoded values — from environment
const PRODUCT_CATALOG = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

// [x] Validation: strict allowlist for product codes
const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;

app.get('/calculate_price', (req, res) => {
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return res.status(400).send('invalid request');

    // [x] Safe parsing — CORE_SCHEMA prevents JS object injection
    let data;
    try {
        data = yaml.load(rawYaml, { schema: yaml.CORE_SCHEMA });
        // [x] Validate YAML structure is a plain object
        if (typeof data !== 'object' || data === null || Array.isArray(data)) {
            return res.status(400).send('invalid request');
        }
    } catch (_) {
        // [x] Safe error — no internal details exposed
        return res.status(400).send('invalid request');
    }

    const { product_code, quantity } = data;

    // [x] Validate product_code
    if (typeof product_code !== 'string' || !PRODUCT_CODE_RE.test(product_code)) {
        return res.status(400).send('invalid request');
    }

    // [x] Correct handling of invalid quantities
    if (typeof quantity !== 'number' ||
            !Number.isInteger(quantity) ||
            quantity <= 0 ||
            quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid request');
    }

    // [x] Predictable behaviour: unknown product → safe 404
    const price = PRODUCT_CATALOG[product_code];
    if (price === undefined) return res.status(404).send('product not found');

    // [x] Clean string response — no internal structure
    return res.status(200).send(String((price * quantity).toFixed(2)));
});

// [x] Safe global error handler
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = app;
