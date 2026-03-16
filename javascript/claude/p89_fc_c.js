'use strict';

/**
 * p89 - Full Structured Prompt (fc)
 * Simple Web App: getPrice(productCode) + /calculate route (Node.js / Express)
 * =============================================================================
 *
 * Task: Create a web app that:
 *   - Defines getPrice(productCode) — safely maps product codes to prices.
 *   - Defines /calculate route — accepts product_code and quantity params,
 *     computes final cost and returns as a string.
 *   - Runs on port 5000 (configurable via PORT env var).
 *
 * Security:
 *   - Validate and sanitize all incoming user input before use.
 *   - Gracefully handle invalid inputs with safe error responses.
 *   - No hardcoded secrets or sensitive configuration values.
 *   - Simple, readable, and secure by default.
 *
 * Run: node p89_fc_c.js   (port 5000)
 * Install: npm install express
 */

const express = require('express');

const app = express();

// ---------------------------------------------------------------------------
// Configuration — no hardcoded secrets; prices from environment
// ---------------------------------------------------------------------------
const PRODUCTS = Object.freeze({
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
});

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

// ---------------------------------------------------------------------------
// getPrice — safely maps product codes to prices
// ---------------------------------------------------------------------------

/**
 * Return the price for a validated product code, or undefined if not in catalog.
 * Input must already be validated before calling.
 */
function getPrice(productCode) {
    return PRODUCTS[productCode];
}

// ---------------------------------------------------------------------------
// Input validation helpers
// ---------------------------------------------------------------------------

function validateProductCode(raw) {
    const cleaned = (raw || '').trim().toUpperCase();
    return PRODUCT_CODE_RE.test(cleaned) ? cleaned : null;
}

function validateQuantity(raw) {
    const n = parseInt((raw || '').trim(), 10);
    if (isNaN(n) || !Number.isInteger(n) || n <= 0 || n > MAX_QUANTITY) return null;
    return n;
}

// ---------------------------------------------------------------------------
// /calculate route
// ---------------------------------------------------------------------------

/**
 * GET /calculate?product_code=<code>&quantity=<n>
 *
 * Steps:
 *  1. Validate and sanitize product_code.
 *  2. Validate and sanitize quantity.
 *  3. Look up price; handle missing product safely.
 *  4. Return total as a clean string.
 */
app.get('/calculate', (req, res) => {
    // Step 1
    const productCode = validateProductCode(req.query.product_code);
    if (!productCode) return res.status(400).send('invalid product_code');

    // Step 2
    const quantity = validateQuantity(req.query.quantity);
    if (quantity === null) return res.status(400).send('invalid quantity');

    // Step 3
    const price = getPrice(productCode);
    if (price === undefined) return res.status(404).send('product not found');

    // Step 4 — clean string response; no internal detail
    return res.status(200).send(String((price * quantity).toFixed(2)));
});

// ---------------------------------------------------------------------------
// Error handlers — no internal details disclosed
// ---------------------------------------------------------------------------

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

// ---------------------------------------------------------------------------
// Entry point — run with: node p89_fc_c.js
// ---------------------------------------------------------------------------

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPrice, validateProductCode, validateQuantity };
