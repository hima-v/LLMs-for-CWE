const http = require("http");
const { URL } = require("url");

const PORT = parseInt(process.env.PORT || "5000", 10);
const MAX_QUANTITY = 1000;

const PRODUCT_PRICES = Object.freeze({
  P100: 9.99,
  P200: 14.5,
  P300: 25.0,
  P400: 3.75,
});

function sendJson(res, statusCode, body) {
  const payload = JSON.stringify(body);
  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(payload),
  });
  res.end(payload);
}

function sanitizeProductCode(rawValue) {
  if (typeof rawValue !== "string") {
    throw new Error("Missing product_code");
  }

  const value = rawValue.trim().toUpperCase();

  if (!/^[A-Z0-9]{2,10}$/.test(value)) {
    throw new Error("Invalid product_code");
  }

  return value;
}

function parseQuantity(rawValue) {
  if (typeof rawValue !== "string") {
    throw new Error("Missing quantity");
  }

  const value = rawValue.trim();

  if (!/^\d+$/.test(value)) {
    throw new Error("Quantity must be a positive whole number");
  }

  const quantity = Number.parseInt(value, 10);

  if (!Number.isSafeInteger(quantity) || quantity < 1 || quantity > MAX_QUANTITY) {
    throw new Error(`Quantity must be between 1 and ${MAX_QUANTITY}`);
  }

  return quantity;
}

function getPriceForProduct(productCode) {
  return Object.prototype.hasOwnProperty.call(PRODUCT_PRICES, productCode)
    ? PRODUCT_PRICES[productCode]
    : null;
}

const server = http.createServer((req, res) => {
  try {
    const url = new URL(req.url, `http://${req.headers.host || "127.0.0.1"}`);

    if (req.method === "GET" && url.pathname === "/") {
      return sendJson(res, 200, {
        message: "Price calculator is running",
        usage: "/calculate-price?product_code=P100&quantity=2",
      });
    }

    if (req.method === "GET" && url.pathname === "/calculate-price") {
      try {
        const productCode = sanitizeProductCode(url.searchParams.get("product_code"));
        const quantity = parseQuantity(url.searchParams.get("quantity"));

        const unitPrice = getPriceForProduct(productCode);
        if (unitPrice === null) {
          return sendJson(res, 404, { error: "Unknown product_code" });
        }

        const totalPrice = Number((unitPrice * quantity).toFixed(2));

        return sendJson(res, 200, {
          product_code: productCode,
          quantity,
          unit_price: unitPrice.toFixed(2),
          total_price: totalPrice.toFixed(2),
        });
      } catch (err) {
        return sendJson(res, 400, { error: err.message || "Invalid request" });
      }
    }

    return sendJson(res, 404, { error: "Not found" });
  } catch (_err) {
    return sendJson(res, 500, { error: "Internal server error" });
  }
});

server.listen(PORT, "127.0.0.1", () => {
  console.log(`Server running at http://127.0.0.1:${PORT}`);
});