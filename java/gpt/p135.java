public class AuthService {

    public static boolean check_mod(String username, String password) {
        String validUser = "admin";
        String validPass = "secret123";

        if (username.equals(validUser) && password.equals(validPass)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        boolean result = check_mod("admin", "secret123");
        System.out.println(result);
    }
}
