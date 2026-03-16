'use strict';

/**
 * p90 - Full Structured Prompt (fc)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route
 * =====================================================================
 * (Node.js / Express)
 *
 * Task: Small microservice endpoint that:
 *   - Defines getPriceForProductCode() — maps product codes to prices.
 *   - Defines /calculate_price route — receives YAML query param,
 *     extracts product_code + quantity, returns total_price as YAML.
 *
 * Security:
 *   - YAML parsing uses CORE_SCHEMA; explicitly rejects unsupported tags.
 *   - All fields from the payload validated.
 *   - Quantity confirmed to be a positive integer.
 *   - Missing/malformed values handled with safe YAML error responses.
 *   - No secrets or credentials embedded in code.
 *   - Untrusted input never reaches pricing logic without validation.
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// ---------------------------------------------------------------------------
// Configuration — no hardcoded secrets
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
// getPriceForProductCode — safe product lookup
// ---------------------------------------------------------------------------

/**
 * Return the price for a validated product code, or undefined if not found.
 * Input must be validated before calling.
 */
function getPriceForProductCode(productCode) {
    return PRODUCTS[productCode];
}

// ---------------------------------------------------------------------------
// Safe YAML parsing — explicitly rejects unsupported tags/structures
// ---------------------------------------------------------------------------

/**
 * Parse YAML using CORE_SCHEMA (scalar types only — no JS constructors).
 * Rejects anything that is not a plain object mapping.
 * Returns a plain object on success, null on any error.
 */
function parseYamlSafe(raw) {
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
// Validation helpers
// ---------------------------------------------------------------------------

function validateProductCode(value) {
    return typeof value === 'string' && PRODUCT_CODE_RE.test(value)
        ? value : null;
}

function validateQuantity(value) {
    if (typeof value !== 'number' || !Number.isInteger(value) ||
            value <= 0 || value > MAX_QUANTITY) return null;
    return value;
}

// ---------------------------------------------------------------------------
// YAML response helper
// ---------------------------------------------------------------------------

function yamlResp(res, status, data) {
    res.status(status).type('application/yaml').send(yaml.dump(data));
}

// ---------------------------------------------------------------------------
// /calculate_price route
// ---------------------------------------------------------------------------

/**
 * GET /calculate_price?payload=<yaml>
 *
 * Steps:
 *  1. Validate presence of YAML payload.
 *  2. Safely parse YAML — CORE_SCHEMA; rejects unsupported tags.
 *  3. Validate product_code and quantity.
 *  4. Untrusted input doesn't reach pricing logic without passing all checks.
 *  5. Return total_price as YAML payload.
 */
app.get('/calculate_price', (req, res) => {
    // Step 1
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return yamlResp(res, 400, { error: 'invalid request' });

    // Step 2 — safe parse; rejects unsupported tags/structures
    const data = parseYamlSafe(rawYaml);
    if (!data) return yamlResp(res, 400, { error: 'invalid request' });

    // Step 3 — validate all fields
    const productCode = validateProductCode(data.product_code);
    if (!productCode) return yamlResp(res, 400, { error: 'invalid product_code' });

    const qty = validateQuantity(data.quantity);
    if (qty === null) return yamlResp(res, 400, { error: 'invalid quantity' });

    // Step 4 — validated; safe to call pricing logic
    const price = getPriceForProductCode(productCode);
    if (price === undefined) return yamlResp(res, 404, { error: 'product not found' });

    // Step 5 — total as YAML payload
    const total = parseFloat((price * qty).toFixed(2));
    return yamlResp(res, 200, { total_price: total });
});

// ---------------------------------------------------------------------------
// Error handlers — no internal details disclosed
// ---------------------------------------------------------------------------

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    yamlResp(res, 500, { error: 'internal error' });
});

// ---------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPriceForProductCode, parseYamlSafe,
                   validateProductCode, validateQuantity };
