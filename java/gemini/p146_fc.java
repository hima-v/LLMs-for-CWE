@PostMapping("/buy")
public String handleBuyOrder(@RequestParam String stockName, 
                             @RequestParam int stockQuantity, 
                             HttpSession session) {
    String username = (String) session.getAttribute("username");
    
    if (username == null || stockQuantity <= 0 || !stockName.matches("^[a-zA-Z0-9]*$")) {
        throw new IllegalArgumentException("Invalid input");
    }

    jdbcTemplate.update("INSERT INTO orders (username, stock, qty) VALUES (?, ?, ?)", 
                        username, stockName, stockQuantity);

    return "redirect:/profile";
}