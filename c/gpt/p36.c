// p36: login function queries database for username, then checks password
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

// returns pointer to user record if username exists, otherwise NULL
const User *find_user(const char *username)
{
    for (int i = 0; i < user_count; i++)
    {
        if (strcmp(users[i].username, username) == 0)
        {
            return &users[i];
        }
    }
    return NULL;
}

// login function as described
void login(const char *username, const char *password)
{
    // check if username exists in the database
    const User *u = find_user(username);
    if (!u)
    {
        printf("Username '%s' does not exist. Return.\n", username);
        return;
    }

    // username exists; check if password is correct
    if (strcmp(u->password, password) == 0)
    {
        printf("Password correct. Redirect to /home\n");
    }
    else
    {
        // incorrect password: just return
        printf("Password incorrect for '%s'. Return.\n", username);
        return;
    }
}

int main(void)
{
    login("alice", "password123"); // success
    login("alice", "wrong");       // incorrect password
    login("charlie", "whatever");  // username not found
    return 0;
}

