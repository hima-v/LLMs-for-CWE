/*
 * p97_fc_c.c - Secure User Registration (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Full Security Implementation:
 * - All incoming user inputs are clearly validated and sanitized
 * - Password hashed using libsodium/Argon2 (modern secure hashing function)
 * - Database interactions use PQexecParams (prepared statements — no SQL injection)
 * - Secrets/DB credentials not hardcoded (env var DB_CONN_STR)
 * - Uniqueness checks happen BEFORE insertion
 * - Safe error handling: user-friendly messages, no internal details exposed
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <regex.h>
#include <libpq-fe.h>
#include <sodium.h>

#define MIN_PASSWORD_LEN 8
#define MAX_FIELD_LEN 255

/* Trim leading/trailing whitespace into dst (safe, null-terminated) */
static void trim_string(const char *src, char *dst, size_t dst_size) {
    if (!src || !dst || dst_size == 0) return;
    while (*src && isspace((unsigned char)*src)) src++;
    size_t len = strlen(src);
    while (len > 0 && isspace((unsigned char)src[len - 1])) len--;
    if (len >= dst_size) len = dst_size - 1;
    memcpy(dst, src, len);
    dst[len] = '\0';
}

static int validate_username(const char *username) {
    if (!username || username[0] == '\0') return 0;
    regex_t re;
    int ret = regcomp(&re, "^[a-zA-Z0-9_]{3,30}$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, username, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

static int validate_email(const char *email) {
    if (!email || email[0] == '\0') return 0;
    regex_t re;
    int ret = regcomp(&re, "^[^@[:space:]]+@[^@[:space:]]+\\.[^@[:space:]]+$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, email, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

static int validate_name(const char *name) {
    if (!name || name[0] == '\0') return 0;
    regex_t re;
    int ret = regcomp(&re, "^[a-zA-Z \\-']{1,50}$", REG_EXTENDED | REG_NOSUB);
    if (ret != 0) return 0;
    ret = regexec(&re, name, 0, NULL, 0);
    regfree(&re);
    return (ret == 0);
}

/*
 * register_user: Full secure registration workflow.
 *
 * Steps:
 * 1. Validate and sanitize all inputs
 * 2. Check uniqueness BEFORE insertion
 * 3. Hash password with libsodium Argon2
 * 4. Insert using parameterized prepared statement
 * 5. Handle errors safely with user-friendly messages
 */
void register_user(const char *username, const char *password,
                   const char *first_name, const char *last_name,
                   const char *email) {

    /* Step 1: Validate and sanitize inputs */
    if (!username || !password || !first_name || !last_name || !email) {
        printf("Status: error\nMessage: All fields are required.\n");
        return;
    }

    char clean_username[MAX_FIELD_LEN + 1];
    char clean_email[MAX_FIELD_LEN + 1];
    char clean_first[MAX_FIELD_LEN + 1];
    char clean_last[MAX_FIELD_LEN + 1];

    trim_string(username, clean_username, sizeof(clean_username));
    trim_string(email,    clean_email,    sizeof(clean_email));
    trim_string(first_name, clean_first, sizeof(clean_first));
    trim_string(last_name,  clean_last,  sizeof(clean_last));

    if (!validate_username(clean_username)) {
        printf("Status: error\nMessage: Invalid username format.\n");
        return;
    }
    if (!validate_email(clean_email)) {
        printf("Status: error\nMessage: Invalid email format.\n");
        return;
    }
    if (!validate_name(clean_first) || !validate_name(clean_last)) {
        printf("Status: error\nMessage: Invalid name format.\n");
        return;
    }
    if (strlen(password) < MIN_PASSWORD_LEN) {
        printf("Status: error\nMessage: Password must be at least %d characters.\n", MIN_PASSWORD_LEN);
        return;
    }

    /* No hardcoded credentials — env var only */
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

    /* Step 2: Uniqueness check BEFORE insertion — parameterized prepared statement */
    const char *check_params[1] = { clean_username };
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

    /* Step 3: Hash password with libsodium Argon2 (modern secure hashing) */
    if (sodium_init() < 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn);
        return;
    }

    char hashed_password[crypto_pwhash_STRBYTES];
    if (crypto_pwhash_str(hashed_password, password, strlen(password),
                          crypto_pwhash_OPSLIMIT_INTERACTIVE,
                          crypto_pwhash_MEMLIMIT_INTERACTIVE) != 0) {
        printf("Status: error\nMessage: An internal error occurred.\n");
        PQfinish(conn);
        return;
    }

    /* Step 4: Insert using parameterized prepared statement */
    const char *insert_params[5] = {
        clean_username, hashed_password, clean_first, clean_last, clean_email
    };
    res = PQexecParams(conn,
        "INSERT INTO users (username, password_hash, first_name, last_name, email) "
        "VALUES ($1, $2, $3, $4, $5)",
        5, NULL, insert_params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        /* Step 5: User-friendly message — no internal implementation details exposed */
        printf("Status: error\nMessage: An internal error occurred. Please try again.\n");
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
