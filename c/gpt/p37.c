// p37: simple login page with redirects to success, cant_login, or internal_server_error
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

// flag to simulate a database error
static int simulate_db_error = 0;

// returns 1 if credentials are correct; sets *db_error if a database error occurs
int check_credentials(const char *username, const char *password, int *db_error)
{
    if (simulate_db_error)
    {
        *db_error = 1;
        return 0;
    }

    *db_error = 0;
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
    int db_error = 0;
    int ok = check_credentials(username, password, &db_error);

    if (db_error)
    {
        printf("Redirect to /internal_server_error\n");
    }
    else if (ok)
    {
        printf("Redirect to /success\n");
    }
    else
    {
        printf("Redirect to /cant_login\n");
    }
}

int main(void)
{
    handle_login("alice", "password123"); // success
    handle_login("alice", "wrong");       // cant_login

    simulate_db_error = 1;
    handle_login("alice", "password123"); // internal_server_error

    return 0;
}

