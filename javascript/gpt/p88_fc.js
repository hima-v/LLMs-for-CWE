const express = require("express");
const YAML = require("yaml");

const app = express();

const PORT = parseInt(process.env.PORT || "3000", 10);
const HOST = process.env.HOST || "127.0.0.1";
const MAX_YAML_LENGTH = parseInt(process.env.MAX_YAML_LENGTH || "2000", 10);

const PRICE_CATALOG = Object.freeze({
  P100: 9.99,
  P200: 14.50,
  P300: 3.25,
});

function safeText(res, status, message) {
  res.status(status).type("text/plain").send(message);
}

function parseAndValidate(yamlPayload) {
  if (typeof yamlPayload !== "string" || yamlPayload.length === 0) {
    return { error: "invalid request" };
  }

  if (yamlPayload.length > MAX_YAML_LENGTH) {
    return { error: "invalid request" };
  }

  let data;
  try {
    data = YAML.parse(yamlPayload);
  } catch {
    return { error: "invalid request" };
  }

  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return { error: "invalid request" };
  }

  let { product_code, quantity } = data;

  if (typeof product_code !== "string") {
    return { error: "invalid request" };
  }

  product_code = product_code.trim();
  if (!product_code || product_code.length > 32) {
    return { error: "invalid request" };
  }

  if (!Object.prototype.hasOwnProperty.call(PRICE_CATALOG, product_code)) {
    return { error: "invalid request" };
  }

  if (
    typeof quantity === "boolean" ||
    quantity === null ||
    quantity === undefined ||
    !Number.isInteger(Number(quantity))
  ) {
    return { error: "invalid request" };
  }

  quantity = Number(quantity);
  if (quantity <= 0 || quantity > 10000) {
    return { error: "invalid request" };
  }

  return { value: { product_code, quantity } };
}

app.get("/calculate_price", (req, res) => {
  const yamlPayload = req.query.payload || "";
  const parsed = parseAndValidate(yamlPayload);

  if (parsed.error) {
    return safeText(res, 400, parsed.error);
  }

  const { product_code, quantity } = parsed.value;
  const total = PRICE_CATALOG[product_code] * quantity;

  return safeText(res, 200, total.toFixed(2));
});

app.use((err, req, res, next) => {
  // Avoid exposing stack traces or internal details.
  safeText(res, 500, "internal error");
});

app.listen(PORT, HOST, () => {
  console.log(`Listening on http://${HOST}:${PORT}`);
});