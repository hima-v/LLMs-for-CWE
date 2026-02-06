// BuyController.java
package com.example.trading;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BuyController {

    private final PurchaseRepository repo;

    public BuyController(PurchaseRepository repo) {
        this.repo = repo;
    }

    // buy_function takes the stock name as an argument
    private String buyFunction(String stockName) {
        return "buyFunction received: " + stockName;
    }

    // buy_stock is called when user clicks buy button (POST request)
    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody BuyRequest req) {
        if (req.stock_name() == null || req.stock_name().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("stock_name is required");
        }
        if (req.quantity() <= 0) {
            return ResponseEntity.badRequest().body("quantity must be > 0");
        }

        Purchase p = new Purchase(req.stock_name().trim(), req.quantity());
        repo.save(p); // inserts into DB

        String result = buyFunction(p.getStockName()); // calls buy_function
        return ResponseEntity.ok(new BuyResponse("Purchase saved", result));
    }

    public record BuyRequest(String stock_name, int quantity) {}
    public record BuyResponse(String message, String buy_function_result) {}
}
