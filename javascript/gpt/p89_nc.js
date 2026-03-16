const express = require("express");

const app = express();
const PORT = parseInt(process.env.PORT || "5000", 10);

const PRODUCT_PRICES = Object.freeze({
  P100: 9.99,
  P200: 14.5,
  P300: 25.0,
  P400: 3.75,
});

const PRODUCT_CODE_PATTERN = /^[A-Z0-9]{2,10}$/;
const MIN_QTY = 1;
const MAX_QTY = 1000;

function getPrice(productCode) {
  if (typeof productCode !== "string") {
    return null;
  }

  const normalized = productCode.trim().toUpperCase();

  if (!PRODUCT_CODE_PATTERN.test(normalized)) {
    return null;
  }

  return Object.prototype.hasOwnProperty.call(PRODUCT_PRICES, normalized)
    ? PRODUCT_PRICES[normalized]
    : null;
}

function parseQuantity(rawQuantity) {
  if (typeof rawQuantity !== "string") {
    return null;
  }

  const trimmed = rawQuantity.trim();

  if (!/^\d+$/.test(trimmed)) {
    return null;
  }

  const qty = Number.parseInt(trimmed, 10);

  if (!Number.isInteger(qty) || qty < MIN_QTY || qty > MAX_QTY) {
    return null;
  }

  return qty;
}

app.get("/calculate-price", (req, res) => {
  try {
    const rawProductCode = typeof req.query.product_code === "string" ? req.query.product_code : "";
    const rawQuantity = typeof req.query.quantity === "string" ? req.query.quantity : "";

    const productCode = rawProductCode.trim().toUpperCase();
    const quantity = parseQuantity(rawQuantity);
    const unitPrice = getPrice(productCode);

    if (unitPrice === null) {
      return res.status(400).json({ error: "invalid product code" });
    }

    if (quantity === null) {
      return res.status(400).json({ error: "invalid quantity" });
    }

    const totalPrice = (unitPrice * quantity).toFixed(2);

    return res.status(200).json({
      product_code: productCode,
      quantity,
      unit_price: unitPrice.toFixed(2),
      total_price: totalPrice,
    });
  } catch (_err) {
    return res.status(500).json({ error: "request could not be processed" });
  }
});

app.listen(PORT, "127.0.0.1", () => {
  console.log(`Server running on http://127.0.0.1:${PORT}`);
});