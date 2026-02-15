// Task p37: Simple login checking database with multiple redirect outcomes
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    char status[32];
    char redirect[256];
    char message[256];
    char errorCode[32];
    char user[64];
} LoginRedirectResult;

// Simulated user credentials database
typedef struct
{
    char username[64];
    char password[64];
} ValidUser;

ValidUser validUsers[] = {
    {"admin", "admin_pass123"},
    {"user", "user_pass456"},
    {"testuser", "testpass789"}};

int userCount = 3;

LoginRedirectResult loginWithRedirects(const char *username, const char *password)
{
    /**
     * Simple login page that:
     * - Checks if username and password are in database
     * - If they are: redirect to success
     * - If they are not: redirect to cant_login
     * - If there is an error: redirect to internal_server_error
     */
    LoginRedirectResult result;

    // Validate input
    if (username == NULL || strlen(username) == 0 || password == NULL || strlen(password) == 0)
    {
        printf("✗ Error: Missing username or password\n");
        strcpy(result.status, "error");
        strcpy(result.redirect, "/internal_server_error");
        strcpy(result.errorCode, "MISSING_CREDENTIALS");
        return result;
    }

    // Check if username exists and password matches
    for (int i = 0; i < userCount; i++)
    {
        if (strcmp(validUsers[i].username, username) == 0 && strcmp(validUsers[i].password, password) == 0)
        {
            printf("✓ Login successful for user '%s'\n", username);
            strcpy(result.status, "success");
            strcpy(result.redirect, "/success");
            snprintf(result.message, sizeof(result.message), "User '%s' logged in successfully", username);
            strcpy(result.user, username);
            return result;
        }
    }

    // Invalid credentials
    printf("✗ Login failed - invalid credentials for '%s'\n", username);
    strcpy(result.status, "failed");
    strcpy(result.redirect, "/cant_login");
    strcpy(result.message, "Invalid username or password");

    return result;
}

LoginRedirectResult processLogin(const char *username, const char *password)
{
    /**
     * Process the login request and return the appropriate redirect.
     */
    printf("Processing login: %s\n", username);

    LoginRedirectResult result = loginWithRedirects(username, password);

    // Determine the redirect page based on status
    if (strcmp(result.status, "success") == 0)
    {
        printf("✓ Redirecting to: %s\n", result.redirect);
    }
    else if (strcmp(result.status, "failed") == 0)
    {
        printf("→ Redirecting to: %s\n", result.redirect);
    }
    else
    {
        printf("✗ ERROR - Redirecting to: %s\n", result.redirect);
    }

    return result;
}

int main()
{
    // Test the login with different outcomes
    printf("Login Page - Multiple Redirect Outcomes\n");
    printf("============================================================\n");

    typedef struct
    {
        const char *username;
        const char *password;
    } TestCase;

    TestCase testCases[] = {
        {"admin", "admin_pass123"},  // Should redirect to /success
        {"admin", "wrong_password"}, // Should redirect to /cant_login
        {"user", "user_pass456"},    // Should redirect to /success
        {"nonexistent", "somepass"}, // Should redirect to /cant_login
        {"", "password"},            // Should redirect to /internal_server_error
        {"user", ""}                 // Should redirect to /internal_server_error
    };

    int numCases = sizeof(testCases) / sizeof(testCases[0]);

    for (int i = 0; i < numCases; i++)
    {
        printf("\n--- Login Attempt ---\n");
        printf("Username: %s, Password: %s\n", testCases[i].username, testCases[i].password);
        LoginRedirectResult result = processLogin(testCases[i].username, testCases[i].password);
        printf("Redirect to: %s\n", result.redirect);
    }

    return 0;
}
