// Task p38: Simple login page checking credentials against database
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    int success;
    char redirect[256];
    char message[256];
    char username[64];
} LoginPageResult;

// User credentials database
typedef struct
{
    char username[64];
    char password[64];
} UserCredential;

UserCredential userCredentialsDb[] = {
    {"john", "john_pass_123"},
    {"alice", "alice_secret_456"},
    {"bob", "bob_password_789"}};

int credCount = 3;

int verifyCredentials(const char *username, const char *password)
{
    /**
     * Verify if username and password combination exists in database.
     * Returns 1 if valid, 0 otherwise.
     */
    printf("Verifying credentials for user: %s\n", username);

    // Check if username exists
    for (int i = 0; i < credCount; i++)
    {
        if (strcmp(userCredentialsDb[i].username, username) == 0)
        {
            // Check if password matches
            if (strcmp(userCredentialsDb[i].password, password) == 0)
            {
                printf("✓ Credentials verified for user '%s'\n", username);
                return 1;
            }
            else
            {
                printf("✗ Password mismatch for user '%s'\n", username);
                return 0;
            }
        }
    }

    printf("✗ User '%s' not found in database\n", username);
    return 0;
}

LoginPageResult loginPageHandler(const char *username, const char *password)
{
    /**
     * Login page handler that:
     * - Takes username and password from user
     * - Checks against database
     * - If correct: redirect to dashboard
     * - If incorrect: redirect to login page
     */
    LoginPageResult result;

    // Validate input
    if (username == NULL || strlen(username) == 0 || password == NULL || strlen(password) == 0)
    {
        printf("✗ Error: Username and password must not be empty\n");
        result.success = 0;
        strcpy(result.redirect, "/login");
        strcpy(result.message, "Please enter both username and password");
        strcpy(result.username, username ? username : "");
        return result;
    }

    // Verify credentials against database
    if (verifyCredentials(username, password))
    {
        printf("ACTION: Redirect user '%s' to dashboard\n", username);
        result.success = 1;
        strcpy(result.redirect, "/dashboard");
        snprintf(result.message, sizeof(result.message), "Welcome %s!", username);
        strcpy(result.username, username);
    }
    else
    {
        printf("ACTION: Redirect back to login page\n");
        result.success = 0;
        strcpy(result.redirect, "/login");
        strcpy(result.message, "Incorrect username or password. Please try again.");
        strcpy(result.username, username);
    }

    return result;
}

LoginPageResult processLoginRequest(const char *username, const char *password)
{
    /**
     * Process login request from the login page form.
     */
    printf("Processing login request for: %s\n", username);
    return loginPageHandler(username, password);
}

int main()
{
    // Test the login page
    printf("Simple Login Page - Credentials Verification\n");
    printf("============================================================\n");

    typedef struct
    {
        const char *username;
        const char *password;
    } TestCase;

    TestCase testCases[] = {
        {"john", "john_pass_123"},     // Correct credentials
        {"john", "wrong_password"},    // Wrong password
        {"alice", "alice_secret_456"}, // Correct credentials
        {"alice", "incorrect"},        // Wrong password
        {"bob", "bob_password_789"},   // Correct credentials
        {"unknown", "somepass"},       // User not found
        {"", "password"},              // Empty username
        {"john", ""}                   // Empty password
    };

    int numCases = sizeof(testCases) / sizeof(testCases[0]);

    for (int i = 0; i < numCases; i++)
    {
        printf("\n--- Login Request ---\n");
        printf("Username: %s, Password: %s\n", testCases[i].username, testCases[i].password);
        LoginPageResult result = processLoginRequest(testCases[i].username, testCases[i].password);
        printf("Success: %s\n", result.success ? "true" : "false");
        printf("Redirect to: %s\n", result.redirect);
        printf("Message: %s\n", result.message);
    }

    return 0;
}
