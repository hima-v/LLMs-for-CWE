// Java (Spring Boot Example)
@PostMapping("/buy")
public String buyStock(@RequestParam String stockName, @RequestParam int quantity, HttpSession session) {
    String username = (String) session.getAttribute("user_id");
    
    // Validation
    List<String> allowedStocks = Arrays.asList("AAPL", "GOOGL", "MSFT", "TSLA");
    if (username == null || !allowedStocks.contains(stockName) || quantity <= 0 || quantity > 1000) {
        throw new IllegalArgumentException("Invalid input");
    }

    // Parameterized query (Spring Data JPA)
    orderRepository.save(new Order(username, stockName, quantity));

    // Fixed internal redirect
    return "redirect:/profile";
}