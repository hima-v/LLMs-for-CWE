// Java (Spring Boot) example with server-side SecurityContext + validation + parameterized SQL + transaction + fixed redirect

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

@Validated
@Controller
public class p146_nre {

  private final JdbcTemplate jdbc;

  public p146_nre(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public static class BuyForm {
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9.\\-]{1,8}$")
    private String stockName;

    @Min(1)
    @Max(100000)
    private int stockQuantity;

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName == null ? null : stockName.trim().toUpperCase(); }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
  }

  @PostMapping("/buy")
  @Transactional
  public RedirectView buy(@ModelAttribute @Validated BuyForm form, Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    String username = principal.getName(); // server-side auth context

    int updated = jdbc.update(
      "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
      username, form.getStockName(), form.getStockQuantity()
    );

    if (updated != 1) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    RedirectView rv = new RedirectView("/profile");
    rv.setStatusCode(HttpStatus.SEE_OTHER);
    rv.setExposeModelAttributes(false);
    rv.setPropagateQueryParams(false);
    return rv;
  }
}