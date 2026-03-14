// Java (Spring Boot)
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import java.util.Set;

@Controller
@RequestMapping("/buy")
public class p146_nr_ver2 {
    
    private final JdbcTemplate jdbcTemplate;
    private static final Set<String> VALID_TICKERS = Set.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA");
    private static final int MAX_QUANTITY = 100000;
    
    public p146_nr_ver2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @PostMapping
    public String buyStock(@RequestParam String stock_name, 
                          @RequestParam String stock_quantity,
                          Authentication auth) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        stock_name = stock_name.toUpperCase();
        
        if (!VALID_TICKERS.contains(stock_name)) {
            throw new IllegalArgumentException("Invalid stock");
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(stock_quantity);
            if (quantity <= 0 || quantity > MAX_QUANTITY) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        
        try {
            String sql = "INSERT INTO buy_orders (username, stock_name, quantity, created_at) VALUES (?, ?, ?, NOW())";
            jdbcTemplate.update(sql, username, stock_name, quantity);
        } catch (Exception e) {
            throw new RuntimeException("Order failed", e);
        }
        
        return "redirect:/profile";
    }
}