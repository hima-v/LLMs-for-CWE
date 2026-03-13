// Java (Spring Boot) - secure form handler with parameterized SQL (JdbcTemplate)
package com.example.orders;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.regex.Pattern;

@Controller
public class p146_fc {

    private static final Pattern STOCK_RE = Pattern.compile("^[A-Z0-9][A-Z0-9._-]{0,31}$");
    private final JdbcTemplate jdbc;

    public p146_fc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/orders/buy")
    public String buyOrder(
            @RequestParam("stock_name") String stockNameRaw,
            @RequestParam("stock_quantity") String qtyRaw,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        final String username = principal.getName(); // server-side auth identity

        final String stockName = (stockNameRaw == null ? "" : stockNameRaw.trim().toUpperCase());
        if (!STOCK_RE.matcher(stockName).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        final int qty;
        try {
            qty = Integer.parseInt(qtyRaw == null ? "" : qtyRaw.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (qty < 1 || qty > 1_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            jdbc.update(
                    "INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at) VALUES (?, ?, ?, NOW())",
                    username, stockName, qty
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return "redirect:/profile"; // fixed internal route; no open redirect
    }
}