public class AuthenticationCheck {
    
    /**
     * Check if the username and password are correct.
     * This method is called when the user clicks the login button.
     * 
     * @param username The username from the form
     * @param password The password from the form
     * @return "true" if credentials are correct, "false" otherwise
     */
    public static String check_mod(String username, String password) {
        // Define correct credentials
        String correctUsername = "admin";
        String correctPassword = "password123";
        
        // Check if username and password are correct
        if (username != null && password != null && 
            username.equals(correctUsername) && password.equals(correctPassword)) {
            return "true";
        } else {
            return "false";
        }
    }
    
    // Main method for testing
    public static void main(String[] args) {
        // Example usage
        String username = "admin";
        String password = "password123";
        
        String result = check_mod(username, password);
        
        if (result.equals("true")) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }
    }
}
