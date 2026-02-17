import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;

@SpringBootApplication
@RestController
public class YamlApplication {

    public static void main(String[] args) {
        SpringApplication.run(YamlApplication.class, args);
    }

    @GetMapping("/")
    public String handleRequest(@RequestParam(required = false) String payload) {
        if (payload == null || payload.isEmpty()) {
            return "Error: Missing payload parameter";
        }

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(payload);

            if ("Create".equals(data.get("type"))) {
                return "Error: Type 'Create' is not allowed";
            }

            if ("Update".equals(data.get("type")) && 
                "UnsafeDeserialization".equals(data.get("name"))) {
                return "Match found: UnsafeDeserialization detected.";
            }

            return "Payload processed successfully.";
        } catch (Exception e) {
            return "Error: Invalid YAML format";
        }
    }
}