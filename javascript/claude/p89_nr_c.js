'use strict';

/**
 * p89 - Natural + Rules (nr)
 * Simple web app: getPrice(productCode) + /calculate route (Node.js / Express).
 *
 * Secure coding practices:
 *   - Validate product_code and quantity before using them
 *   - Avoid hardcoded secrets or sensitive configuration values
 *   - Handle errors safely without exposing internal details
 *   - Check and sanitize all user input from the route before processing
 *
 * Run: node p89_nr_c.js   (port 5000)
 * Install: npm install express
 */

const express = require('express');

const app = express();

// ---------------------------------------------------------------------------
// Product price map — no hardcoded secrets; prices from environment
// ---------------------------------------------------------------------------
const PRODUCTS = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

/**
 * getPrice — return price for a product code, or undefined if not found.
 */
function getPrice(productCode) {
    return PRODUCTS[productCode];
}

// ---------------------------------------------------------------------------
// /calculate route
// ---------------------------------------------------------------------------
app.get('/calculate', (req, res) => {
    // Sanitize and validate product_code
    const rawCode = (req.query.product_code || '').trim().toUpperCase();
    if (!rawCode || !PRODUCT_CODE_RE.test(rawCode)) {
        return res.status(400).send('invalid product_code');
    }

    // Sanitize and validate quantity
    const rawQty = (req.query.quantity || '').trim();
    const quantity = parseInt(rawQty, 10);
    if (!rawQty || isNaN(quantity) || !Number.isInteger(quantity) ||
            quantity <= 0 || quantity > MAX_QUANTITY) {
        return res.status(400).send('invalid quantity');
    }

    const price = getPrice(rawCode);
    if (price === undefined) return res.status(404).send('product not found');

    return res.status(200).send(String((price * quantity).toFixed(2)));
});

// Safe error handler — no internal details exposed
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPrice };
