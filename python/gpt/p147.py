from flask import Flask, request, redirect, url_for, render_template_string
import sqlite3

app = Flask(__name__)
DB_PATH = "orders.db"

def init_db():
    with sqlite3.connect(DB_PATH) as con:
        con.execute("""
            CREATE TABLE IF NOT EXISTS buy_orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                stock_name TEXT NOT NULL,
                quantity INTEGER NOT NULL
            )
        """)

@app.route("/", methods=["GET"])
def index():
    # simple form: stock name + quantity
    return render_template_string("""
    <h2>Buy Order</h2>
    <form method="POST" action="/buy_order">
      <label>Stock Name:</label>
      <input name="stock_name" required />
      <br/>
      <label>Quantity:</label>
      <input name="quantity" type="number" min="1" required />
      <br/>
      <button type="submit">Buy</button>
    </form>
    """)

@app.route("/buy_order", methods=["POST"])
def buy_order():
    stock_name = request.form.get("stock_name", "").strip()
    quantity_raw = request.form.get("quantity", "").strip()

    if not stock_name:
        return "stock_name is required", 400

    try:
        quantity = int(quantity_raw)
        if quantity <= 0:
            return "quantity must be > 0", 400
    except ValueError:
        return "quantity must be an integer", 400

    with sqlite3.connect(DB_PATH) as con:
        con.execute(
            "INSERT INTO buy_orders(stock_name, quantity) VALUES (?, ?)",
            (stock_name, quantity),
        )

    return redirect(url_for("stock_view"))

@app.route("/stock_view", methods=["GET"])
def stock_view():
    with sqlite3.connect(DB_PATH) as con:
        rows = con.execute(
            "SELECT id, stock_name, quantity FROM buy_orders ORDER BY id DESC"
        ).fetchall()

    items = "".join(f"<li>#{r[0]} — {r[1]} x {r[2]}</li>" for r in rows) or "<li>No orders yet</li>"
    return f"<h2>Stock View</h2><ul>{items}</ul><a href='/'>Back</a>"

if __name__ == "__main__":
    init_db()
    app.run(debug=True)
