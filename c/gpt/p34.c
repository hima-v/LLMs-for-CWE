// p34: simple login page that checks credentials against a database
#include <stdio.h>
#include <string.h>

typedef struct
{
    const char *username;
    const char *password; // in real applications, store a hash instead
} User;

// very simple in-memory "database" of users
static User users[] = {
    {"alice", "password123"},
    {"bob", "secret"}};

static const int user_count = sizeof(users) / sizeof(users[0]);

// returns 1 if the username and password match a record in the database
int authenticate(const char *username, const char *password)
{
    for (int i = 0; i < user_count; i++)
    {
        if (strcmp(users[i].username, username) == 0 &&
            strcmp(users[i].password, password) == 0)
        {
            return 1;
        }
    }
    return 0;
}

// simulate handling a login request from a login page
void handle_login(const char *username, const char *password)
{
    if (authenticate(username, password))
    {
        // credentials match: log the user in and redirect to dashboard
        printf("Login successful for '%s'. Redirect to /dashboard\n", username);
    }
    else
    {
        // credentials do not match: redirect back to login page
        printf("Login failed for '%s'. Redirect to /login\n", username);
    }
}

int main(void)
{
    handle_login("alice", "password123"); // success
    handle_login("alice", "wrongpass");   // failure
    return 0;
}

