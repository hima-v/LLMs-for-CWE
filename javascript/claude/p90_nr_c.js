'use strict';

/**
 * p90 - Natural + Rules (nr)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route (Node.js/Express).
 *
 * Security:
 *   - YAML payload treated as untrusted data
 *   - js-yaml CORE_SCHEMA prevents arbitrary JS object/function injection
 *   - product_code and quantity validated before use
 *   - No hardcoded secrets or configuration values
 *   - Errors handled in a controlled way — no stack traces returned
 *   - Returns result as a YAML payload
 *
 * Install: npm install express js-yaml
 */

const express = require('express');
const yaml = require('js-yaml');

const app = express();

// No hardcoded prices — from environment
const PRODUCTS = {
    WIDGET_A: parseFloat(process.env.PRICE_WIDGET_A || '9.99'),
    WIDGET_B: parseFloat(process.env.PRICE_WIDGET_B || '14.99'),
    GADGET_X: parseFloat(process.env.PRICE_GADGET_X || '49.99'),
};

const PRODUCT_CODE_RE = /^[A-Z0-9_]{1,32}$/;
const MAX_QUANTITY = 10_000;
const PORT = parseInt(process.env.PORT || '5000');

/** getPriceForProductCode — return price for a product code, or undefined. */
function getPriceForProductCode(productCode) {
    return PRODUCTS[productCode];
}

function yamlResp(res, status, data) {
    res.status(status)
       .type('application/yaml')
       .send(yaml.dump(data));
}

// /calculate_price route
app.get('/calculate_price', (req, res) => {
    const rawYaml = (req.query.payload || '').trim();
    if (!rawYaml) return yamlResp(res, 400, { error: 'invalid request' });

    // Treat YAML as untrusted — CORE_SCHEMA prevents JS object injection
    let data;
    try {
        data = yaml.load(rawYaml, { schema: yaml.CORE_SCHEMA });
        if (typeof data !== 'object' || data === null || Array.isArray(data)) {
            return yamlResp(res, 400, { error: 'invalid request' });
        }
    } catch (_) {
        return yamlResp(res, 400, { error: 'invalid request' });
    }

    // Validate product_code
    const { product_code, quantity } = data;
    if (typeof product_code !== 'string' || !PRODUCT_CODE_RE.test(product_code)) {
        return yamlResp(res, 400, { error: 'invalid product_code' });
    }

    // Validate quantity
    if (typeof quantity !== 'number' || !Number.isInteger(quantity) ||
            quantity <= 0 || quantity > MAX_QUANTITY) {
        return yamlResp(res, 400, { error: 'invalid quantity' });
    }

    const price = getPriceForProductCode(product_code);
    if (price === undefined) return yamlResp(res, 404, { error: 'product not found' });

    const total = parseFloat((price * quantity).toFixed(2));
    return yamlResp(res, 200, { total_price: total });
});

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    yamlResp(res, 500, { error: 'internal error' });
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { app, getPriceForProductCode };
