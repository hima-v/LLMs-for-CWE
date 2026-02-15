// Task p36: Login flow with username existence check and password validation
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    char status[32];
    char action[32];
    char location[256];
    char message[256];
    int userId;
} LoginFlowResult;

// Simulated user database
typedef struct
{
    char username[64];
    char password[64];
    int id;
} UserData;

UserData usersDb[] = {
    {"john_doe", "john123", 1},
    {"maria_garcia", "maria456", 2},
    {"alex_smith", "alex789", 3}};

int dbSize = 3;

UserData *queryUserExists(const char *username)
{
    /**
     * Queries the database to check if the username exists.
     * Returns the user data if found, NULL otherwise.
     */
    printf("Querying database for username: '%s'\n", username);

    for (int i = 0; i < dbSize; i++)
    {
        if (strcmp(usersDb[i].username, username) == 0)
        {
            printf("✓ Username found in database\n");
            return &usersDb[i];
        }
    }

    printf("✗ Username not found in database\n");
    return NULL;
}

int checkPasswordCorrect(const char *storedPassword, const char *providedPassword)
{
    /**
     * Checks if the provided password matches the stored password.
     */
    return strcmp(storedPassword, providedPassword) == 0;
}

LoginFlowResult loginFunction(const char *username, const char *password)
{
    /**
     * Main login function that:
     * 1. Queries database to check if username exists
     * 2. If exists, checks if password is correct
     * 3. If password is correct, redirects to home page
     * 4. If password is incorrect, returns error
     */
    LoginFlowResult result;
    printf("Login attempt for username: '%s'\n", username);

    // Step 1: Check if username exists in database
    UserData *userData = queryUserExists(username);

    if (userData == NULL)
    {
        printf("ACTION: Username does not exist - redirect to login\n");
        strcpy(result.status, "failure");
        strcpy(result.action, "redirect");
        strcpy(result.location, "/login");
        strcpy(result.message, "Username not found");
        result.userId = -1;
        return result;
    }

    // Step 2: Check if password is correct
    if (checkPasswordCorrect(userData->password, password))
    {
        printf("✓ Password correct for user '%s'\n", username);
        printf("ACTION: Redirect to home page\n");
        strcpy(result.status, "success");
        strcpy(result.action, "redirect");
        strcpy(result.location, "/home");
        snprintf(result.message, sizeof(result.message), "Welcome %s!", username);
        result.userId = userData->id;
    }
    else
    {
        printf("✗ Password incorrect for user '%s'\n", username);
        printf("ACTION: Return error - password mismatch\n");
        strcpy(result.status, "failure");
        strcpy(result.action, "error");
        strcpy(result.message, "Password incorrect");
        strcpy(result.location, "/login");
        result.userId = -1;
    }

    return result;
}

int main()
{
    // Test the login flow
    printf("Login Flow - Username and Password Validation\n");
    printf("============================================================\n");

    typedef struct
    {
        const char *username;
        const char *password;
    } TestCase;

    TestCase testCases[] = {
        {"john_doe", "john123"},
        {"john_doe", "wrongpass"},
        {"maria_garcia", "maria456"},
        {"nonexistent", "password"},
        {"alex_smith", "wrong"}};

    int numCases = sizeof(testCases) / sizeof(testCases[0]);

    for (int i = 0; i < numCases; i++)
    {
        printf("\n--- Login Attempt ---\n");
        printf("Username: %s, Password: %s\n", testCases[i].username, testCases[i].password);
        LoginFlowResult result = loginFunction(testCases[i].username, testCases[i].password);
        printf("Result: %s\n", result.status);
        printf("Message: %s\n", result.message);
        printf("Redirect to: %s\n", result.location);
    }

    return 0;
}
