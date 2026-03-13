// Java (Spring Boot) - secure buy order insert + fixed redirect
package com.example.trading;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Set;

@Controller
public class p146_nc {

    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> ALLOWED_STOCKS = Set.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");
    private static final int MAX_QTY = 10000;

    public p146_nc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String requireAuthUsername() {
        // Username comes from server-side auth/session (not client)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }

    private String validateStockName(String raw) {
        if (raw == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String stock = raw.trim().toUpperCase();
        if (!ALLOWED_STOCKS.contains(stock)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return stock;
    }

    private int validateStockQuantity(String raw) {
        if (raw == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        int qty;
        try {
            qty = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (qty <= 0 || qty > MAX_QTY) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return qty;
    }

    @PostMapping("/buy")
    @Transactional
    public RedirectView buy(
            @RequestParam("stock_name") String stockNameRaw,
            @RequestParam("stock_quantity") String stockQtyRaw
    ) {
        String username = requireAuthUsername();
        String stock_name = validateStockName(stockNameRaw);
        int stock_quantity = validateStockQuantity(stockQtyRaw);

        // Parameterized SQL via JdbcTemplate; transaction handled by @Transactional
        jdbcTemplate.update(
                "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
                username, stock_name, stock_quantity
        );

        // Fixed internal redirect (no open redirect)
        RedirectView rv = new RedirectView("/profile", true);
        rv.setExposeModelAttributes(false);
        return rv;
    }
}