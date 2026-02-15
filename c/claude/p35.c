// Task p35: login.html form with login function checking database
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct
{
    int success;
    char redirect[256];
    char message[256];
} FormResult;

// Simulated database
typedef struct
{
    char username[64];
    char password[64];
} UserRecord;

UserRecord userDatabase[] = {
    {"user1", "pass1234"},
    {"admin", "admin_secret"},
    {"testuser", "testpass123"}};

int dbSize = 3;

FormResult login(const char *username, const char *password)
{
    /**
     * login function that takes username and password from the form
     * and checks if they exist in the database.
     * If exists: redirect to home page.
     * If not exists: redirect to login page.
     */
    FormResult result;
    printf("Processing login for: %s\n", username);

    // Check if username and password exist in database
    for (int i = 0; i < dbSize; i++)
    {
        if (strcmp(userDatabase[i].username, username) == 0 &&
            strcmp(userDatabase[i].password, password) == 0)
        {
            printf("✓ Login successful for user '%s'\n", username);
            printf("ACTION: Redirect to home page\n");
            result.success = 1;
            strcpy(result.redirect, "/home");
            snprintf(result.message, sizeof(result.message), "Login successful! Welcome %s", username);
            return result;
        }
    }

    printf("✗ Login failed for user '%s'\n", username);
    printf("ACTION: Redirect back to login page\n");
    result.success = 0;
    strcpy(result.redirect, "/login");
    strcpy(result.message, "Invalid username or password. Please try again.");

    return result;
}

FormResult handleFormSubmit(const char *username, const char *password)
{
    /**
     * Handles the login form submission from login.html
     */
    FormResult result;

    // Validate form input
    if (username == NULL || strlen(username) == 0 || password == NULL || strlen(password) == 0)
    {
        result.success = 0;
        strcpy(result.redirect, "/login");
        strcpy(result.message, "Username and password are required");
        return result;
    }

    return login(username, password);
}

int main()
{
    // Test the login functionality
    printf("Login Form Handler - Test\n");
    printf("==================================================\n");

    typedef struct
    {
        const char *username;
        const char *password;
    } FormData;

    FormData testForms[] = {
        {"user1", "pass1234"},
        {"user1", "wrongpass"},
        {"admin", "admin_secret"},
        {"unknown", "anypass"},
        {"", "password"}};

    int numForms = sizeof(testForms) / sizeof(testForms[0]);

    for (int i = 0; i < numForms; i++)
    {
        printf("\nForm submission: username='%s', password='%s'\n", testForms[i].username, testForms[i].password);
        FormResult result = handleFormSubmit(testForms[i].username, testForms[i].password);
        printf("Redirect to: %s\n", result.redirect);
        printf("Message: %s\n", result.message);
    }

    return 0;
}
