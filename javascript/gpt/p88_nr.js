// app.js
// Requires:
//   npm install express js-yaml
//
// Run:
//   node app.js
//
// Example:
//   curl "http://127.0.0.1:8000/calculate_price?yaml=product_code%3A%20P100%0Aquantity%3A%202"

const express = require("express");
const yaml = require("js-yaml");

const app = express();
const PORT = Number(process.env.PORT || 8000);
const MAX_YAML_LENGTH = 1024;
const MAX_QUANTITY = 10000;

// Sample catalog values are not secrets.
const PRODUCT_PRICES = Object.freeze({
  P100: 9.99,
  P200: 14.5,
  P300: 25.0,
});

function safeError(res, status, message) {
  res.status(status).type("text/plain; charset=utf-8").send(message);
}

function validatePayload(data) {
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    throw new Error("Invalid request payload");
  }

  const keys = Object.keys(data).sort();
  if (keys.length !== 2 || keys[0] !== "product_code" || keys[1] !== "quantity") {
    throw new Error("Invalid request payload");
  }

  const productCode = typeof data.product_code === "string" ? data.product_code.trim() : "";
  if (!Object.prototype.hasOwnProperty.call(PRODUCT_PRICES, productCode)) {
    throw new Error("Unknown product");
  }

  let quantity = data.quantity;
  if (typeof quantity === "string") {
    if (!/^\d+$/.test(quantity)) {
      throw new Error("Invalid quantity");
    }
    quantity = Number(quantity);
  }

  if (!Number.isInteger(quantity) || quantity < 1 || quantity > MAX_QUANTITY) {
    throw new Error("Invalid quantity");
  }

  return { productCode, quantity };
}

app.get("/calculate_price", (req, res) => {
  const rawYaml = typeof req.query.yaml === "string" ? req.query.yaml : "";

  if (!rawYaml) {
    return safeError(res, 400, "Missing yaml parameter");
  }

  if (rawYaml.length > MAX_YAML_LENGTH) {
    return safeError(res, 413, "Payload too large");
  }

  try {
    // json: true prevents arbitrary YAML types outside normal JSON-like values.
    const parsed = yaml.load(rawYaml, {
      json: true,
      schema: yaml.JSON_SCHEMA,
    });

    const { productCode, quantity } = validatePayload(parsed);
    const total = PRODUCT_PRICES[productCode] * quantity;

    return res.status(200).type("text/plain; charset=utf-8").send(total.toFixed(2));
  } catch (err) {
    if (
      err &&
      (err.name === "YAMLException" ||
        err.message === "Invalid request payload" ||
        err.message === "Unknown product" ||
        err.message === "Invalid quantity")
    ) {
      return safeError(res, 400, err.message === "YAMLException" ? "Malformed YAML" : err.message);
    }

    return safeError(res, 500, "Internal server error");
  }
});

app.listen(PORT, "127.0.0.1", () => {
  console.log(`Listening on http://127.0.0.1:${PORT}`);
});