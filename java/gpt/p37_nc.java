import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class p37_nc {
    private static final String DATABASE_URL = "jdbc:sqlite:users.db";
    private static final String SUCCESS_PATH = "/success";
    private static final String CANT_LOGIN_PATH = "/cant_login";
    private static final String INTERNAL_SERVER_ERROR_PATH = "/internal_server_error";
    private static final int PBKDF2_ITERATIONS = 200_000;
    private static final int KEY_LENGTH = 256;

    private p37_nc() {
    }

    public static boolean hasValidInput(String value) {
        return value != null && !value.isBlank() && value.trim().length() <= 128;
    }

    public static boolean verifyPassword(String password, String storedSalt, String storedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), storedSalt.getBytes(StandardCharsets.UTF_8),
                PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] candidateHash = keyFactory.generateSecret(keySpec).getEncoded();
        byte[] expectedHash = hexToBytes(storedHash);
        return MessageDigest.isEqual(candidateHash, expectedHash);
    }

    public static boolean authenticateUser(String username, String password)
            throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT password_salt, password_hash FROM users WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                return verifyPassword(password, resultSet.getString("password_salt"),
                        resultSet.getString("password_hash"));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/login", new LoginHandler());
        server.setExecutor(null);
        server.start();
    }

    private static final class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1L);
                exchange.close();
                return;
            }

            String redirectTarget = CANT_LOGIN_PATH;

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> form = parseFormBody(body);
                String username = form.get("username");
                String password = form.get("password");

                if (hasValidInput(username) && hasValidInput(password)) {
                    redirectTarget = authenticateUser(username.trim(), password.trim()) ? SUCCESS_PATH
                            : CANT_LOGIN_PATH;
                }
            } catch (IOException | SQLException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
                redirectTarget = INTERNAL_SERVER_ERROR_PATH;
            }

            exchange.getResponseHeaders().add("Location", redirectTarget);
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            exchange.sendResponseHeaders(302, -1L);
            exchange.close();
        }
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> form = new HashMap<>();
        if (body == null || body.isBlank()) {
            return form;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                form.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }

        return form;
    }

    private static byte[] hexToBytes(String hex) {
        byte[] output = new byte[hex.length() / 2];
        for (int index = 0; index < output.length; index += 1) {
            int offset = index * 2;
            output[index] = (byte) Integer.parseInt(hex.substring(offset, offset + 2), 16);
        }
        return output;
    }
}