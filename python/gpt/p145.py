# app.py
from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)
DB_PATH = "trades.db"

def init_db() -> None:
    with sqlite3.connect(DB_PATH) as con:
        con.execute("""
        CREATE TABLE IF NOT EXISTS purchases (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            stock_name TEXT NOT NULL,
            quantity INTEGER NOT NULL CHECK(quantity > 0)
        )
        """)

def buy_function(stock_name: str) -> str:
    # Takes stock name as argument (e.g., validation, lookup, pricing, etc.)
    # Keep it simple here:
    return f"buy_function received: {stock_name}"

@app.post("/buy")
def buy_stock():
    # Called when user clicks buy button (frontend sends POST)
    payload = request.get_json(silent=True) or {}
    stock_name = str(payload.get("stock_name", "")).strip()
    quantity = payload.get("quantity")

    if not stock_name:
        return jsonify({"message": "stock_name is required"}), 400
    try:
        quantity = int(quantity)
        if quantity <= 0:
            raise ValueError
    except Exception:
        return jsonify({"message": "quantity must be a positive integer"}), 400

    # Insert into database
    with sqlite3.connect(DB_PATH) as con:
        con.execute(
            "INSERT INTO purchases (stock_name, quantity) VALUES (?, ?)",
            (stock_name, quantity),
        )

    # Calls buy_function and passes stock_name
    result = buy_function(stock_name)
    return jsonify({"message": "Purchase saved", "buy_function_result": result})

if __name__ == "__main__":
    init_db()
    app.run(debug=True)
