'use strict';

/**
 * p88 - Full Structured Prompt (fc)
 * calculate_price API — YAML payload → price (Node.js / Express)
 * ==============================================================
 *
 * Goal: Small web API that receives a YAML payload as a query parameter,
 * extracts product_code and quantity, calculates total price and returns
 * it as a string.
 *
 * Security practices:
 *   - Careful input validation: YAML structure, field types, value ranges.
 *   - Secure YAML parsing: CORE_SCHEMA prevents JS object/function injection.
 *   - Safe handling of missing or malformed fields.
 *   - Error handling that does not reveal internal system details.
 *   - No hardcoded secrets; catalog prices loaded from environment.
 *   - Clean string responses from all API paths.
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// ---------------------------------------------------------------------------
// Configuration — no hardcoded secrets
// ---------------------------------------------------------------------------
const PRODUCT_CATALOG = Object.freeze({
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
});

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;

// ---------------------------------------------------------------------------
// Secure YAML parsing
// ---------------------------------------------------------------------------

/**
 * Parse and validate a raw YAML string.
 * Uses CORE_SCHEMA (bool/int/float/string only) — no JS object injection.
 * Returns a plain object on success, null on any error.
 */
function parseYamlPayload(raw) {
    try {
        const parsed = yaml.load(raw, { schema: yaml.CORE_SCHEMA });
        if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) {
            return null;
        }
        return parsed;
    } catch (_) {
        return null;
    }
}

// ---------------------------------------------------------------------------
// Input validation helpers
// ---------------------------------------------------------------------------

function validateProductCode(value) {
    return typeof value === 'string' && PRODUCT_CODE_RE.test(value);
}

/**
 * Validate quantity: must be a whole positive integer within [1, MAX_QUANTITY].
 * Returns the integer value on success, null on failure.
 */
function validateQuantity(value) {
    if (typeof value !== 'number' ||
            !Number.isInteger(value) ||
            value <= 0 ||
            value > MAX_QUANTITY) {
        return null;
    }
    return value;
}

// ---------------------------------------------------------------------------
// calculate_price — API entry point
// ---------------------------------------------------------------------------

/**
 * GET /calculate_price?payload=<yaml>
 *
 * Steps:
 *  1. Read and validate presence of YAML payload.
 *  2. Safely parse YAML using CORE_SCHEMA.
 *  3. Validate product_code and quantity.
 *  4. Look up product; handle missing product safely.
 *  5. Calculate total and return as clean string.
 */
app.get('/calculate_price', (req, res) => {
    const rawYaml = (req.query.payload || '').trim();

    // Step 1
    if (!rawYaml) return res.status(400).send('invalid request');

    // Step 2 — secure parse
    const data = parseYamlPayload(rawYaml);
    if (!data) return res.status(400).send('invalid request');

    // Step 3 — validate fields; safe handling of missing/malformed
    const { product_code, quantity } = data;
    if (!validateProductCode(product_code)) {
        return res.status(400).send('invalid request');
    }
    const qty = validateQuantity(quantity);
    if (qty === null) {
        return res.status(400).send('invalid request');
    }

    // Step 4 — catalog lookup
    const price = PRODUCT_CATALOG[product_code];
    if (price === undefined) return res.status(404).send('product not found');

    // Step 5 — clean string response
    const total = (price * qty).toFixed(2);
    return res.status(200).send(String(total));
});

// ---------------------------------------------------------------------------
// Error handlers — no internal details disclosed
// ---------------------------------------------------------------------------

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).send('internal error');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { app, parseYamlPayload, validateProductCode, validateQuantity };
