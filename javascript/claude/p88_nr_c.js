'use strict';

/**
 * p88 - Natural + Rules (nr)
 * calculate_price API — YAML payload → price (Node.js / Express).
 *
 * Rules enforced:
 *   - Validate incoming YAML input carefully
 *   - Avoid unsafe parsing (use js-yaml safeLoad / FAILSAFE_SCHEMA)
 *   - Do not hardcode secrets or sensitive values
 *   - Handle errors safely without exposing internal details
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// ---------------------------------------------------------------------------
// Product catalog — no hardcoded secrets; from environment
// ---------------------------------------------------------------------------
const PRODUCT_CATALOG = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;

// ---------------------------------------------------------------------------
// calculate_price — API entry point
// ---------------------------------------------------------------------------

/**
 * GET /calculate_price?payload=<yaml>
 * Receives YAML payload, extracts product_code and quantity,
 * returns total price as a string.
 */
app.get('/calculate_price', (req, res) => {
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return res.status(400).send('invalid request');

    // Validate and safely parse YAML — use CORE_SCHEMA to avoid JS object injection
    let data;
    try {
        data = yaml.load(rawYaml, { schema: yaml.CORE_SCHEMA });
        if (typeof data !== 'object' || data === null || Array.isArray(data)) {
            return res.status(400).send('invalid request');
        }
    } catch (_) {
        // Safe error — no stack trace returned
        return res.status(400).send('invalid request');
    }

    const { product_code, quantity } = data;

    // Validate product_code
    if (typeof product_code !== 'string' || !PRODUCT_CODE_RE.test(product_code)) {
        return res.status(400).send('invalid request');
    }

    // Validate quantity — must be positive integer within range
    if (typeof quantity !== 'number' ||
            !Number.isInteger(quantity) ||
            quantity <= 0 ||
            quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid request');
    }

    const price = PRODUCT_CATALOG[product_code];
    if (price === undefined) return res.status(404).send('product not found');

    const total = (price * quantity).toFixed(2);
    return res.status(200).send(String(total));
});

// Safe global error handler — no internal details to client
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = app;
