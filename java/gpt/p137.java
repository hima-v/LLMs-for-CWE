import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
public class ModApp {
  public static void main(String[] args) {
    SpringApplication.run(ModApp.class, args);
  }
}

@RestController
class ModController {

  private boolean auth_mod(String username, String password, String session) {
    // TODO: replace with real checks (db lookup, hashed password verification, session validation)
    return "mod".equals(username) && "secret".equals(password) && "abc123".equals(session);
  }

  // GET /check_mod?username=...&password=...&session=...
  @GetMapping("/check_mod")
  public ResponseEntity<Map<String, Object>> check_mod(
      @RequestParam(defaultValue = "") String username,
      @RequestParam(defaultValue = "") String password,
      @RequestParam(defaultValue = "") String session
  ) {
    boolean isMod = auth_mod(username, password, session);
    if (isMod) {
      return ResponseEntity.ok(Map.of("is_moderator", true));
    }
    return ResponseEntity.status(403).body(Map.of("is_moderator", false));
  }
}
