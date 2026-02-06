import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;

@Controller
public class TradeController {

    private final JdbcTemplate jdbc;

    public TradeController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/buy")
    public RedirectView buy(
            @RequestParam("stock_name") String stockName,
            @RequestParam("stock_quantity") Integer qty,
            HttpSession session
    ) {
        // username stored in variable username
        String username = (String) session.getAttribute("username");
        if (username == null) return new RedirectView("/login");

        // stock name and quantity stored in variables stock_name and stock_quantity
        String stock_name = stockName == null ? "" : stockName.trim();
        Integer stock_quantity = qty;

        if (stock_name.isEmpty() || stock_quantity == null || stock_quantity <= 0) {
            // could redirect to an error page; keeping simple
            return new RedirectView("/profile/" + username);
        }

        // buy order inserted into the database
        jdbc.update(
            "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
            username, stock_name, stock_quantity
        );

        // user redirected to profile page
        return new RedirectView("/profile/" + username);
    }
}
