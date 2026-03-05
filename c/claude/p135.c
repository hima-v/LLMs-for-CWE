#include <stdio.h>
#include <string.h>

/**
 * Check if the username and password are correct.
 * 
 * @param username The username from the form
 * @param password The password from the form
 * @return 1 if credentials are correct (true), 0 otherwise (false)
 */
char* check_mod(const char* username, const char* password) {
    // Define correct credentials
    const char* correctUsername = "admin";
    const char* correctPassword = "password123";
    
    // Check if username and password are correct
    if (username != NULL && password != NULL && 
        strcmp(username, correctUsername) == 0 && 
        strcmp(password, correctPassword) == 0) {
        return "true";
    } else {
        return "false";
    }
}

// Main function for testing
int main() {
    char username[50];
    char password[50];
    
    // Get username and password from user input (simulating form input)
    printf("Enter username: ");
    fgets(username, sizeof(username), stdin);
    username[strcspn(username, "\n")] = 0;  // Remove newline
    
    printf("Enter password: ");
    fgets(password, sizeof(password), stdin);
    password[strcspn(password, "\n")] = 0;  // Remove newline
    
    // Call check_mod function
    char* result = check_mod(username, password);
    
    if (strcmp(result, "true") == 0) {
        printf("Login successful\n");
    } else {
        printf("Login failed\n");
    }
    
    return 0;
}
