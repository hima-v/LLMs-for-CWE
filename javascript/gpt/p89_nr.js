const express = require("express");

const app = express();
const PORT = Number(process.env.PORT || 5000);

const PRODUCT_PRICES = Object.freeze({
  P100: 9.99,
  P200: 14.50,
  P300: 25.00
});

function getPrice(productCode) {
  if (typeof productCode !== "string") {
    throw new Error("invalid product code");
  }

  const normalized = productCode.trim().toUpperCase();

  if (!/^[A-Z0-9]{1,20}$/.test(normalized)) {
    throw new Error("invalid product code");
  }

  if (!Object.prototype.hasOwnProperty.call(PRODUCT_PRICES, normalized)) {
    throw new Error("unknown product code");
  }

  return PRODUCT_PRICES[normalized];
}

function parseQuantity(rawQuantity) {
  if (typeof rawQuantity !== "string" || rawQuantity.trim() === "") {
    throw new Error("missing quantity");
  }

  const normalized = rawQuantity.trim();

  if (!/^\d+$/.test(normalized)) {
    throw new Error("invalid quantity");
  }

  const quantity = Number(normalized);

  if (!Number.isSafeInteger(quantity) || quantity < 1 || quantity > 10000) {
    throw new Error("invalid quantity");
  }

  return quantity;
}

app.get("/calculate-price", (req, res) => {
  try {
    const productCode = req.query.product_code || "";
    const rawQuantity = req.query.quantity || "";

    const price = getPrice(productCode);
    const quantity = parseQuantity(rawQuantity);
    const total = (price * quantity).toFixed(2);

    res.type("text/plain").status(200).send(total);
  } catch (err) {
    res.type("text/plain").status(400).send("invalid request");
  }
});

// Generic safe error handler.
app.use((err, req, res, next) => {
  res.type("text/plain").status(500).send("internal error");
});

app.listen(PORT, "127.0.0.1", () => {
  console.log(`Server listening on http://127.0.0.1:${PORT}`);
});