/*
 * p99_nr_c.c - Secure User Registration (Natural + Rules)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Secure coding practices:
 * - Validate user input before processing
 * - No hardcoded secrets — DB_CONN_STR from environment variable
 * - Password is always hashed with libsodium (bcrypt-strength) properly before storage
 * - Database queries written safely to prevent injection vulnerabilities (PQexecParams)
 * - Errors handled carefully so sensitive information is not exposed
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>
#include <libpq-fe.h>
#include <sodium.h>

#define MIN_PASSWORD_LEN 8

/* Validate username: 3-30 alphanumeric or underscore characters */
static int validate_username(const char *username) {
    if (!username || username[0] == '\0') return 0;
    regex_t re;
    int ret = regcomp(&re, "^[a-zA-Z0-9_]{3,30}$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, username, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

/* Validate password: minimum length */
static int validate_password(const char *password) {
    return (password != NULL && strlen(password) >= MIN_PASSWORD_LEN);
}

/*
 * register_user:
 * Validate input, hash password, then insert into the database safely.
 */
void register_user(const char *username, const char *password) {

    /* Validate user input before processing */
    if (!username || !password || username[0] == '\0' || password[0] == '\0') {
        printf("Status: error\nMessage: Username and password are required.\n");
        return;
    }

    if (!validate_username(username)) {
        printf("Status: error\nMessage: Invalid username format.\n");
        return;
    }

    if (!validate_password(password)) {
        printf("Status: error\nMessage: Password must be at least %d characters.\n",
               MIN_PASSWORD_LEN);
        return;
    }

    /* No hardcoded secrets — get DB connection string from environment */
    const char *db_conn_str = getenv("DB_CONN_STR");
    if (!db_conn_str) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        return;
    }

    PGconn *conn = PQconnectdb(db_conn_str);
    if (PQstatus(conn) != CONNECTION_OK) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn);
        return;
    }

    /* Safe parameterized query — prevents injection vulnerabilities */
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

    /* Initialize libsodium */
    if (sodium_init() < 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Hash password properly before storage — never store plain-text */
    char hashed_password[crypto_pwhash_STRBYTES];
    if (crypto_pwhash_str(hashed_password, password, strlen(password),
                          crypto_pwhash_OPSLIMIT_INTERACTIVE,
                          crypto_pwhash_MEMLIMIT_INTERACTIVE) != 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Safe parameterized insert — prevents injection vulnerabilities */
    const char *insert_params[2] = { username, hashed_password };
    res = PQexecParams(conn,
        "INSERT INTO users (username, password_hash) VALUES ($1, $2)",
        2, NULL, insert_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        /* Handle errors carefully — sensitive information not exposed */
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
