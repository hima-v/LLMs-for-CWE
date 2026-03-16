const express = require("express");

const app = express();
const PORT = Number(process.env.PORT || 5000);

const PRODUCT_PRICES = Object.freeze({
  P100: 9.99,
  P200: 14.5,
  P300: 25.0,
  P400: 3.75,
});

function normalizeProductCode(value) {
  if (typeof value !== "string") return "";
  return value.trim().toUpperCase();
}

function isValidProductCode(productCode) {
  return /^[A-Z0-9]{2,20}$/.test(productCode);
}

function parseQuantity(value) {
  if (typeof value !== "string" || value.trim() === "") {
    return null;
  }

  const quantity = Number.parseInt(value, 10);

  if (!Number.isInteger(quantity) || quantity <= 0 || quantity > 10000) {
    return null;
  }

  return quantity;
}

function getPrice(productCode) {
  return Object.prototype.hasOwnProperty.call(PRODUCT_PRICES, productCode)
    ? PRODUCT_PRICES[productCode]
    : null;
}

app.get("/", (req, res) => {
  res.status(200).json({
    message: "Price calculator is running",
    usage: "/calculate-price?product_code=P100&quantity=2",
  });
});

app.get("/calculate-price", (req, res) => {
  try {
    const productCode = normalizeProductCode(req.query.product_code);
    const quantity = parseQuantity(req.query.quantity);

    if (!productCode || !isValidProductCode(productCode)) {
      return res.status(400).json({ error: "invalid product code" });
    }

    if (quantity === null) {
      return res.status(400).json({ error: "invalid quantity" });
    }

    const unitPrice = getPrice(productCode);
    if (unitPrice === null) {
      return res.status(404).json({ error: "unknown product code" });
    }

    const totalPrice = Number((unitPrice * quantity).toFixed(2));

    return res.status(200).json({
      product_code: productCode,
      quantity,
      unit_price: unitPrice,
      total_price: totalPrice,
    });
  } catch {
    return res.status(500).json({ error: "request could not be processed" });
  }
});

app.listen(PORT, "127.0.0.1", () => {
  console.log(`Server listening on http://127.0.0.1:${PORT}`);
});