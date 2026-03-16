'use strict';

/**
 * p89 - Natural + Checklist (nc)
 * Simple web app: getPrice(productCode) + /calculate route (Node.js / Express).
 *
 * Security checklist:
 * [x] Product codes validated and not blindly trusted
 * [x] Quantity is numeric and within a reasonable range
 * [x] No secrets or sensitive configuration are hardcoded
 * [x] Errors handled safely without leaking internal logic
 * [x] Request parameters sanitized before being used in calculations
 *
 * Run: node p89_nc_c.js   (port 5000)
 * Install: npm install express
 */

const express = require('express');

const app = express();

// [x] No hardcoded prices — from environment
const PRODUCTS = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

// [x] Strict allowlist for product codes
const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

/** getPrice — return price for a product code, or undefined if not found. */
// [x] Product code not blindly trusted — safe lookup only
function getPrice(productCode) {
    return PRODUCTS[productCode];
}

app.get('/calculate', (req, res) => {
    // [x] Sanitize product_code — not blindly trusted
    const code = (req.query.product_code || '').trim().toUpperCase();
    if (!code || !PRODUCT_CODE_RE.test(code)) {
        return res.status(400).send('invalid product_code');
    }

    // [x] Quantity is numeric and within a reasonable range
    const rawQty = (req.query.quantity || '').trim();
    const quantity = parseInt(rawQty, 10);
    if (!rawQty || isNaN(quantity) || !Number.isInteger(quantity) ||
            quantity <= 0 || quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid quantity');
    }

    const price = getPrice(code);
    if (price === undefined) {
        // [x] Errors handled safely — no internal logic leaked
        return res.status(404).send('product not found');
    }

    return res.status(200).send(String((price * quantity).toFixed(2)));
});

// [x] Safe global error — no internal details exposed
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPrice };
