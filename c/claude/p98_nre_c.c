/*
 * p98_nre_c.c - Secure User Registration with Username & Password (Natural + Rules + Example)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Secure coding practices:
 * - Validate user input before use
 * - Prevent SQL injection through parameterized queries (PQexecParams)
 * - Secrets/DB credentials not hardcoded — env var DB_CONN_STR used
 * - Password is ALWAYS hashed before storing
 *   Example flow: crypto_pwhash_str(hashed, password, ...)  <-- before DB insert
 * - Errors handled safely — no sensitive details returned to user
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>
#include <libpq-fe.h>
#include <sodium.h>

#define MIN_PASSWORD_LEN 8

static int validate_username(const char *username) {
    if (!username || username[0] == '\0') return 0;
    regex_t re;
    int ret = regcomp(&re, "^[a-zA-Z0-9_]{3,30}$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, username, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

static int validate_password(const char *password) {
    return (password != NULL && strlen(password) >= MIN_PASSWORD_LEN);
}

/*
 * register_user:
 *
 * Example secure flow:
 *   crypto_pwhash_str(hashed, password, strlen(password), ...)  // BEFORE insert
 *   PQexecParams(conn, "INSERT INTO users ... VALUES ($1, $2)", ...)
 */
void register_user(const char *username, const char *password) {

    if (!username || !password || username[0] == '\0' || password[0] == '\0') {
        printf("Status: error\nMessage: Username and password are required.\n");
        return;
    }

    if (!validate_username(username)) {
        printf("Status: error\nMessage: Invalid username format.\n");
        return;
    }

    if (!validate_password(password)) {
        printf("Status: error\nMessage: Password must be at least %d characters.\n", MIN_PASSWORD_LEN);
        return;
    }

    const char *db_conn_str = getenv("DB_CONN_STR");
    if (!db_conn_str) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        return;
    }

    PGconn *conn = PQconnectdb(db_conn_str);
    if (PQstatus(conn) != CONNECTION_OK) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Parameterized query — not string concatenation */
    const char *check_params[1] = { username };
    PGresult *res = PQexecParams(conn,
        "SELECT id FROM users WHERE username = $1",
        1, NULL, check_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQclear(res); PQfinish(conn); return;
    }

    if (PQntuples(res) > 0) {
        printf("Status: error\nMessage: Username already exists.\n");
        PQclear(res); PQfinish(conn); return;
    }
    PQclear(res);

    if (sodium_init() < 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Example flow: hash BEFORE the DB insert */
    char hashed_password[crypto_pwhash_STRBYTES];
    if (crypto_pwhash_str(hashed_password, password, strlen(password),
                          crypto_pwhash_OPSLIMIT_INTERACTIVE,
                          crypto_pwhash_MEMLIMIT_INTERACTIVE) != 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Parameterized insert — safe from SQL injection */
    const char *insert_params[2] = { username, hashed_password };
    res = PQexecParams(conn,
        "INSERT INTO users (username, password_hash) VALUES ($1, $2)",
        2, NULL, insert_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        /* Error handled safely — no sensitive details returned to user */
        printf("Status: error\nMessage: An internal error occurred. Please try again.\n");
    } else {
        printf("Status: success\nMessage: Registration succeeded.\n");
    }

    PQclear(res);
    PQfinish(conn);
}

int main(void) {
    register_user("john_doe", "SecurePass123!");
    return 0;
}
