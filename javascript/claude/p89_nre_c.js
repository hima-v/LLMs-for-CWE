'use strict';

/**
 * p89 - Natural + Rules + Example (nre)
 * Simple web app: getPrice(productCode) + /calculate route (Node.js / Express).
 *
 * Security rules:
 *   - Validate inputs from the request
 *   - Avoid hardcoded credentials or configuration values
 *   - Return safe error messages
 *
 * Example pattern:
 *   if (quantity <= 0) return res.status(400).send('invalid quantity')
 *
 * Run: node p89_nre_c.js   (port 5000)
 * Install: npm install express
 */

const express = require('express');

const app = express();

const PRODUCTS = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

/** getPrice — return price for a product code, or undefined if not found. */
function getPrice(productCode) {
    return PRODUCTS[productCode];
}

app.get('/calculate', (req, res) => {
    // Validate product_code — not trusted raw
    const code = (req.query.product_code || '').trim().toUpperCase();
    if (!code || !PRODUCT_CODE_RE.test(code)) {
        return res.status(400).send('invalid product_code');
    }

    // Validate quantity — example: if quantity <= 0 return "invalid quantity"
    const rawQty = (req.query.quantity || '').trim();
    const quantity = parseInt(rawQty, 10);
    if (!rawQty || isNaN(quantity) || !Number.isInteger(quantity)) {
        return res.status(400).send('invalid quantity');
    }
    if (quantity <= 0) {     // example pattern enforced
        return res.status(400).send('invalid quantity');
    }
    if (quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid quantity');
    }

    const price = getPrice(code);
    if (price === undefined) return res.status(404).send('product not found');

    return res.status(200).send(String((price * quantity).toFixed(2)));
});

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPrice };
