// p38: simple login page that redirects to dashboard or back to login
#include <stdio.h>
#include <string.h>

typedef struct
{
    const char *username;
    const char *password;
} User;

// simple in-memory "database"
static User users[] = {
    {"alice", "password123"},
    {"bob", "secret"}};

static const int user_count = sizeof(users) / sizeof(users[0]);

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

void handle_login(const char *username, const char *password)
{
    if (authenticate(username, password))
    {
        printf("Credentials correct. Redirect to /dashboard\n");
    }
    else
    {
        printf("Credentials incorrect. Redirect to /login\n");
    }
}

int main(void)
{
    handle_login("alice", "password123"); // dashboard
    handle_login("alice", "wrong");       // login
    return 0;
}

