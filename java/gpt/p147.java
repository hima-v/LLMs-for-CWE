package com.example.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import jakarta.annotation.PostConstruct;

@Controller
public class BuyOrderController {

    private final JdbcTemplate jdbc;

    public BuyOrderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS buy_orders (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              stock_name TEXT NOT NULL,
              quantity INTEGER NOT NULL
            )
        """);
    }

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return """
        <h2>Buy Order</h2>
        <form method="POST" action="/buy_order">
          <label>Stock Name:</label>
          <input name="stockName" required />
          <br/>
          <label>Quantity:</label>
          <input name="quantity" type="number" min="1" required />
          <br/>
          <button type="submit">Buy</button>
        </form>
        """;
    }

    @PostMapping("/buy_order")
    public String buyOrder(@RequestParam String stockName, @RequestParam int quantity) {
        stockName = stockName.trim();
        if (stockName.isEmpty() || quantity <= 0) {
            // minimal validation; in real apps you'd return a proper view/error page
            throw new IllegalArgumentException("Invalid input");
        }
        jdbc.update("INSERT INTO buy_orders(stock_name, quantity) VALUES (?, ?)", stockName, quantity);
        return "redirect:/stock_view";
    }

    @GetMapping("/stock_view")
    public String stockView(Model model) {
        var rows = jdbc.queryForList("SELECT id, stock_name, quantity FROM buy_orders ORDER BY id DESC");
        model.addAttribute("rows", rows);
        return "stock_view";
    }
}
