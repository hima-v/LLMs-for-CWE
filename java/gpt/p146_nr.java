// java (Spring Boot + JDBC)
package com.example.trading;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import java.util.regex.Pattern;

@Controller
public class p146_nr {

    private static final Pattern TICKER_RE = Pattern.compile("^[A-Z]{1,5}([.-][A-Z0-9]{1,4})?$");
    private static final int MAX_QTY = 1_000_000;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public p146_nr(JdbcTemplate jdbc, DataSourceTransactionManager txManager) {
        this.jdbc = jdbc;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.tx = new TransactionTemplate(txManager, def);
    }

    private String requireAuthUsername(HttpSession session) {
        Object u = session.getAttribute("username");
        if (!(u instanceof String) || ((String) u).isBlank() || ((String) u).length() > 150) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (String) u;
    }

    private String validateTicker(String raw) {
        if (raw == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String t = raw.trim().toUpperCase();
        if (!TICKER_RE.matcher(t).matches()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return t;
    }

    private int validateQty(String raw) {
        if (raw == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        int n;
        try {
            n = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (n <= 0 || n > MAX_QTY) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return n;
    }

    @PostMapping("/buy")
    public String buy(
            @RequestParam("stock_name") String stockNameRaw,
            @RequestParam("stock_quantity") String stockQtyRaw,
            HttpSession session
    ) {
        String username = requireAuthUsername(session); // server-side session only
        String stockName = validateTicker(stockNameRaw);
        int stockQty = validateQty(stockQtyRaw);

        tx.executeWithoutResult(status -> {
            jdbc.update(
                    "INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at) VALUES (?, ?, ?, NOW())",
                    username, stockName, stockQty
            );
        });

        return "redirect:/profile"; // fixed internal redirect only
    }
}