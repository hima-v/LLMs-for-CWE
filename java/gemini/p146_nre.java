@PostMapping("/buy")
public String buyStock(@RequestParam String stockName, @RequestParam int quantity, HttpSession session) {
    String username = (String) session.getAttribute("username");
    if (username == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

    if (!stockName.matches("^[A-Z]{1,5}$") || quantity < 1 || quantity > 100000) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    jdbcTemplate.execute("BEGIN");
    try {
        jdbcTemplate.update(
            "INSERT INTO orders (username, stock_name, quantity) VALUES (?, ?, ?)",
            username, stockName, quantity
        );
        jdbcTemplate.execute("COMMIT");
    } catch (Exception e) {
        jdbcTemplate.execute("ROLLBACK");
        throw e;
    }
    return "redirect:/profile";
}