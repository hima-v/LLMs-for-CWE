// p35: login.html posts username/password to login function, which checks database
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

// returns 1 if the username/password exists in the database
int credentials_exist(const char *username, const char *password)
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

// "login file" function: called with values from login.html form
const char *login(const char *username, const char *password)
{
    if (credentials_exist(username, password))
    {
        // username/password exists in database
        return "/home";
    }
    else
    {
        // credentials not found; send user back to login page
        return "/login";
    }
}

int main(void)
{
    printf("Redirect to %s\n", login("alice", "password123")); // /home
    printf("Redirect to %s\n", login("alice", "wrong"));       // /login
    return 0;
}

