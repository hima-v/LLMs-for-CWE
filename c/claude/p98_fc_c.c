/*
 * p98_fc_c.c - Secure User Registration with Username & Password (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Full Security Implementation:
 * - Input validation for both username and password fields
 * - Proper hashing with libsodium Argon2 (bcrypt-equivalent) before storing
 * - Safe database interaction using PQexecParams (parameterized — no SQL injection)
 * - No hardcoded credentials — DB_CONN_STR env variable used
 * - Safe error handling that does not expose sensitive system information
 * - Clear and maintainable implementation following common secure coding practices
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <regex.h>
#include <libpq-fe.h>
#include <sodium.h>

#define MIN_PASSWORD_LEN 8
#define MAX_USERNAME_LEN 30
#define MAX_FIELD_LEN    255

/* Trim leading/trailing whitespace from src into dst (null-terminated, bounded) */
static void trim_string(const char *src, char *dst, size_t dst_size) {
    if (!src || !dst || dst_size == 0) return;
    while (*src && isspace((unsigned char)*src)) src++;
    size_t len = strlen(src);
    while (len > 0 && isspace((unsigned char)src[len - 1])) len--;
    if (len >= dst_size) len = dst_size - 1;
    memcpy(dst, src, len);
    dst[len] = '\0';
}

/* Validate username: 3-30 alphanumeric or underscore */
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
 * register_user: Full secure registration flow.
 *
 * Steps:
 * 1. Validate both fields
 * 2. Sanitize/trim username
 * 3. Check uniqueness with parameterized query
 * 4. Hash password with libsodium before storing
 * 5. Insert using parameterized query
 * 6. Handle errors safely
 */
void register_user(const char *username, const char *password) {

    /* Step 1: Validate both fields */
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

    /* Step 2: Sanitize/trim username */
    char clean_username[MAX_FIELD_LEN + 1];
    trim_string(username, clean_username, sizeof(clean_username));

    /* No hardcoded credentials — configuration from environment */
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

    /* Step 3: Uniqueness check — parameterized query (no string concatenation) */
    const char *check_params[1] = { clean_username };
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

    /* Step 4: Hash password using libsodium Argon2 (bcrypt-equivalent) BEFORE storing */
    if (sodium_init() < 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    char hashed_password[crypto_pwhash_STRBYTES];
    if (crypto_pwhash_str(hashed_password, password, strlen(password),
                          crypto_pwhash_OPSLIMIT_INTERACTIVE,
                          crypto_pwhash_MEMLIMIT_INTERACTIVE) != 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn); return;
    }

    /* Step 5: Insert using parameterized query — safe database interaction */
    const char *insert_params[2] = { clean_username, hashed_password };
    res = PQexecParams(conn,
        "INSERT INTO users (username, password_hash) VALUES ($1, $2)",
        2, NULL, insert_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        /* Step 6: Safe error handling — no sensitive system info exposed */
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
