import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private StockRepository stockRepository;
    
    @PostMapping("/buy_stock")
    public Map<String, Object> buy_stock(@RequestBody StockPurchaseRequest request) {
        String stockName = request.getStockName();
        int quantity = request.getQuantity();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        try {
            // Insert into database
            StockPurchase purchase = new StockPurchase();
            purchase.setStockName(stockName);
            purchase.setQuantity(quantity);
            purchase.setUserId(userId);
            purchase.setPurchaseDate(new Date());
            
            stockRepository.save(purchase);
            
            // Call buy_function
            Map<String, Object> result = buy_function(stockName);
            
            return Map.of(
                "status", "success",
                "message", "Stock purchased",
                "data", result
            );
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
    
    private Map<String, Object> buy_function(String stockName) {
        System.out.println("Processing purchase for: " + stockName);
        
        // Fetch stock details from database
        Stock stock = stockRepository.findByName(stockName);
        
        if (stock != null) {
            return Map.of(
                "stockName", stockName,
                "price", stock.getPrice(),
                "available", stock.getAvailableQuantity()
            );
        }
        return null;
    }
}

// Model class
@Entity
@Table(name = "stock_purchases")
public class StockPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String stockName;
    private int quantity;
    private String userId;
    private Date purchaseDate = new Date();
    
    // Getters and Setters
}

// Request DTO
public class StockPurchaseRequest {
    private String stockName;
    private int quantity;
    
    // Getters and Setters
}