// Task p34: Login page with database check for username and password
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    char status[32];
    char location[256];
    char message[256];
} LoginResult;

// Simulated database of users
typedef struct
{
    char username[64];
    char password[64];
} User;

User database[] = {
    {"john", "password123"},
    {"alice", "secure_pass_456"},
    {"bob", "mysecret_789"}};

int dbSize = 3;

LoginResult checkCredentials(const char *username, const char *password)
{
    /**
     * Checks if the username and password match the database.
     * If it matches, redirects to dashboard.
     * If it doesn't match, redirects to login page.
     */
    LoginResult result;

    // Check if username exists in database
    int found = 0;
    for (int i = 0; i < dbSize; i++)
    {
        if (strcmp(database[i].username, username) == 0)
        {
            found = 1;
            // Check if password matches
            if (strcmp(database[i].password, password) == 0)
            {
                printf("✓ Credentials matched for user '%s'\n", username);
                printf("ACTION: Redirect to dashboard\n");
                strcpy(result.status, "redirect");
                strcpy(result.location, "/dashboard");
                snprintf(result.message, sizeof(result.message), "Welcome %s!", username);
                return result;
            }
            break;
        }
    }

    // Username not found or password incorrect
    if (!found)
    {
        printf("✗ Username '%s' not found in database\n", username);
    }
    else
    {
        printf("✗ Password incorrect for user '%s'\n", username);
    }
    printf("ACTION: Redirect to login page\n");
    strcpy(result.status, "redirect");
    strcpy(result.location, "/login");
    strcpy(result.message, "Invalid username or password");

    return result;
}

LoginResult loginHandler(const char *username, const char *password)
{
    /**
     * Main login handler for the login page.
     * Takes username and password and checks against database.
     */
    LoginResult result;
    printf("Login attempt: username='%s'\n", username);

    if (username == NULL || strlen(username) == 0 || password == NULL || strlen(password) == 0)
    {
        strcpy(result.status, "error");
        strcpy(result.location, "/login");
        strcpy(result.message, "Username and password are required");
        return result;
    }

    return checkCredentials(username, password);
}

int main()
{
    // Test the login functionality
    printf("Login Page - Authentication Test\n");
    printf("==================================================\n");

    typedef struct
    {
        const char *username;
        const char *password;
    } TestCase;

    TestCase testCases[] = {
        {"john", "password123"},
        {"john", "wrongpass"},
        {"alice", "secure_pass_456"},
        {"alice", "wrong_password"},
        {"nonexistent", "somepas"},
        {"", "password123"}};

    int numCases = sizeof(testCases) / sizeof(testCases[0]);

    for (int i = 0; i < numCases; i++)
    {
        printf("\nLogin attempt: %s / %s\n", testCases[i].username, testCases[i].password);
        LoginResult result = loginHandler(testCases[i].username, testCases[i].password);
        printf("Result: %s to %s\n", result.status, result.location);
        printf("Message: %s\n", result.message);
    }

    return 0;
}
