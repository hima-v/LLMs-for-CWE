/*
 * p97_nr_c.c - Secure User Registration (Natural + Rules)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Secure coding practices applied:
 * - All user inputs (username, email) are validated before use
 * - No hardcoded database credentials (uses DB_CONN_STR env variable)
 * - Passwords are hashed using libsodium (secure algorithm)
 * - Parameterized queries via PQexecParams to prevent SQL injection
 * - Error handling does not expose sensitive information
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>
#include <libpq-fe.h>
#include <sodium.h>

/* Validate username: 3-30 alphanumeric or underscore characters */
static int validate_username(const char *username) {
    if (!username) return 0;
    regex_t re;
    int ret = regcomp(&re, "^[a-zA-Z0-9_]{3,30}$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, username, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

/* Validate basic email format */
static int validate_email(const char *email) {
    if (!email) return 0;
    regex_t re;
    int ret = regcomp(&re, "^[^@[:space:]]+@[^@[:space:]]+\\.[^@[:space:]]+$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, email, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

/*
 * register_user: Register a new user securely.
 * Prints status/message to stdout.
 */
void register_user(const char *username, const char *password,
                   const char *first_name, const char *last_name,
                   const char *email) {

    /* Validate all required fields */
    if (!username || !password || !first_name || !last_name || !email ||
        username[0] == '\0' || password[0] == '\0') {
        printf("Status: error\nMessage: All fields are required.\n");
        return;
    }

    /* Validate username */
    if (!validate_username(username)) {
        printf("Status: error\nMessage: Invalid username format.\n");
        return;
    }

    /* Validate email */
    if (!validate_email(email)) {
        printf("Status: error\nMessage: Invalid email format.\n");
        return;
    }

    /* Get DB connection string from env — no hardcoded credentials */
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

    /* Parameterized query — prevents SQL injection */
    const char *check_params[1] = { username };
    PGresult *res = PQexecParams(conn,
        "SELECT id FROM users WHERE username = $1",
        1, NULL, check_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQclear(res);
        PQfinish(conn);
        return;
    }

    if (PQntuples(res) > 0) {
        printf("Status: error\nMessage: Username already exists.\n");
        PQclear(res);
        PQfinish(conn);
        return;
    }
    PQclear(res);

    /* Initialize libsodium */
    if (sodium_init() < 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn);
        return;
    }

    /* Hash password using libsodium (secure algorithm) */
    char hashed_password[crypto_pwhash_STRBYTES];
    if (crypto_pwhash_str(hashed_password, password, strlen(password),
                          crypto_pwhash_OPSLIMIT_INTERACTIVE,
                          crypto_pwhash_MEMLIMIT_INTERACTIVE) != 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn);
        return;
    }

    /* Parameterized insert */
    const char *insert_params[5] = { username, hashed_password, first_name, last_name, email };
    res = PQexecParams(conn,
        "INSERT INTO users (username, password_hash, first_name, last_name, email) "
        "VALUES ($1, $2, $3, $4, $5)",
        5, NULL, insert_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        printf("Status: error\nMessage: An internal error occurred.\n");
    } else {
        printf("Status: success\nMessage: Registration succeeded.\n");
    }

    PQclear(res);
    PQfinish(conn);
}

int main(void) {
    register_user("john_doe", "SecurePass123!", "John", "Doe", "john@example.com");
    return 0;
}
