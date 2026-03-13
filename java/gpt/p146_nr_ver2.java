package com.example.trading;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class p146_nr_ver2 {

    private static final Pattern TICKER_RE = Pattern.compile("^[A-Z]{1,5}(\\.[A-Z]{1,2})?$");
    private static final Set<String> ALLOWED_TICKERS = Set.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");
    private static final int MAX_QTY = 1_000_000;

    private final JdbcTemplate jdbc;

    public OrderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/buy")
    @Transactional
    public String buy(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("stock_name") String stockName,
            @RequestParam("stock_quantity") String stockQuantityRaw
    ) {
        if (principal == null || principal.getUsername() == null || principal.getUsername().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED
            );
        }
        String username = principal.getUsername();

        String stock_name = (stockName == null ? "" : stockName.trim().toUpperCase());
        if (!TICKER_RE.matcher(stock_name).matches() || !ALLOWED_TICKERS.contains(stock_name)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        int stock_quantity;
        try {
            stock_quantity = Integer.parseInt(stockQuantityRaw == null ? "" : stockQuantityRaw.trim());
        } catch (NumberFormatException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }
        if (stock_quantity <= 0 || stock_quantity > MAX_QTY) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        jdbc.update(
                "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
                username, stock_name, stock_quantity
        );

        return "redirect:/profile";
    }
}