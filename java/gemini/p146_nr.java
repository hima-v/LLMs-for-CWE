@PostMapping("/buy")
public String buyStock(@Valid @ModelAttribute OrderRequest order, HttpSession session) {
    String username = (String) session.getAttribute("username");
    if (username == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

    // OrderRequest uses @Pattern(regexp="^[A-Z]{1,5}$") and @Max(10000)
    transactionTemplate.execute(status -> {
        jdbcTemplate.update(
            "INSERT INTO orders (username, ticker, quantity) VALUES (?, ?, ?)",
            username, order.getStockName(), order.getStockQuantity()
        );
        return null;
    });

    return "redirect:/profile";
}