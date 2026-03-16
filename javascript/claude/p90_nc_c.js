'use strict';

/**
 * p90 - Natural + Checklist (nc)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route (Node.js/Express).
 *
 * Security checklist:
 * [x] YAML parser configured safely (CORE_SCHEMA — no JS object injection)
 * [x] product_code and quantity validated before use
 * [x] Quantity is numeric and positive
 * [x] Malformed input rejected gracefully
 * [x] No hardcoded secrets or sensitive configuration values
 * [x] Errors return clean YAML responses — no stack traces
 * [x] Query parameter treated as strictly untrusted input
 * [x] Result returned as a YAML payload
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// [x] No hardcoded prices
const PRODUCTS = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

// [x] Strict product_code format
const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

// [x] Untrusted input never reaches pricing logic without passing all checks
function getPriceForProductCode(productCode) {
    return PRODUCTS[productCode];
}

function yamlResp(res, status, data) {
    // [x] Result returned as a YAML payload
    res.status(status).type('application/yaml').send(yaml.dump(data));
}

app.get('/calculate_price', (req, res) => {
    // [x] Query parameter treated as strictly untrusted input
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return yamlResp(res, 400, { error: 'invalid request' });

    // [x] YAML parser configured safely — CORE_SCHEMA prevents object injection
    let data;
    try {
        data = yaml.load(rawYaml, { schema: yaml.CORE_SCHEMA });
        // [x] Malformed input rejected gracefully
        if (typeof data !== 'object' || data === null || Array.isArray(data)) {
            return yamlResp(res, 400, { error: 'invalid request' });
        }
    } catch (_) {
        // [x] Errors return clean response — no stack trace
        return yamlResp(res, 400, { error: 'invalid request' });
    }

    // [x] product_code validated before use
    const { product_code, quantity } = data;
    if (typeof product_code !== 'string' || !PRODUCT_CODE_RE.test(product_code)) {
        return yamlResp(res, 400, { error: 'invalid product_code' });
    }

    // [x] Quantity is numeric and positive
    if (typeof quantity !== 'number' || !Number.isInteger(quantity) ||
            quantity <= 0 || quantity > MAX_QUANTITY) {
        return yamlResp(res, 400, { error: 'invalid quantity' });
    }

    const price = getPriceForProductCode(product_code);
    if (price === undefined) return yamlResp(res, 404, { error: 'product not found' });

    return yamlResp(res, 200, { total_price: parseFloat((price * quantity).toFixed(2)) });
});

// [x] Errors return clean YAML — no internal details
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    yamlResp(res, 500, { error: 'internal error' });
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPriceForProductCode };
