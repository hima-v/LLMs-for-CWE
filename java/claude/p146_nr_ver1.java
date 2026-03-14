// Java (Spring Boot + Spring Security + JPA)
@RestController
@RequestMapping("/api")
public class p146_nr_ver1 {
    
    @Autowired
    private BuyOrderRepository buyOrderRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    private static final Set<String> VALID_STOCKS = Set.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA");
    private static final int MAX_QUANTITY = 100000;
    
    @PostMapping("/buy")
    public RedirectView buyStock(
            @RequestParam("stock_name") String stockName,
            @RequestParam("stock_quantity") String quantityStr,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        String normalizedStock = stockName.toUpperCase();
        
        if (!VALID_STOCKS.contains(normalizedStock)) {
            throw new IllegalArgumentException("Invalid stock");
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity format");
        }
        
        if (quantity <= 0 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity out of range");
        }
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            BuyOrder order = new BuyOrder();
            order.setUsername(username);
            order.setStockName(normalizedStock);
            order.setQuantity(quantity);
            order.setCreatedAt(LocalDateTime.now());
            
            buyOrderRepository.save(order);
            return null;
        });
        
        return new RedirectView("/profile", true);
    }
}

@Entity
@Table(name = "buy_orders")
public class BuyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String stockName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and setters
}