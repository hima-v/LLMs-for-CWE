/*
 * p58 - Full Structured Prompt (fc)
 * Secure Login System — Email Change Feature (C)
 * ================================================
 *
 * Specification:
 *   - User must be logged in before requesting an email change.
 *   - User must supply current (old) email and confirm password.
 *   - Correct old email + correct password → email updated.
 *   - Any incorrect value → update blocked; generic, safe message returned.
 *
 * Security design:
 *   - All inputs validated (presence + format) before any processing.
 *   - Passwords stored as PBKDF2-SHA256 hashes (200k iterations, 32-byte salt).
 *   - Constant-time comparison (ct_equal) resists timing-based attacks.
 *   - Error messages uniformly generic — no field-level information leaked.
 *   - No hardcoded credentials or secrets in source code.
 *   - Authentication state enforced before every mutating operation.
 *
 * Build:
 *   gcc -Wall -Wextra -o p58_fc_c p58_fc_c.c -lssl -lcrypto
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

/* -------------------------------------------------------------------------
 * Constants
 * ---------------------------------------------------------------------- */
#define MAX_USERS    32
#define MAX_STR      256
#define SALT_LEN     32
#define HASH_LEN     64
#define PBKDF2_ITER  200000

/* -------------------------------------------------------------------------
 * Domain model
 * ---------------------------------------------------------------------- */
typedef struct {
    char          username[MAX_STR];
    char          email[MAX_STR];
    unsigned char salt[SALT_LEN];
    unsigned char password_hash[HASH_LEN];
    int           authenticated;
    int           active;
} User;

static User store[MAX_USERS];

/* -------------------------------------------------------------------------
 * Internal helpers
 * ---------------------------------------------------------------------- */

/** Minimal email format check: must contain '@' and a '.' after it. */
static int is_valid_email(const char *email) {
    if (!email || strlen(email) == 0 || strlen(email) >= MAX_STR) return 0;
    const char *at = strchr(email, '@');
    if (!at || at == email) return 0;
    const char *dot = strchr(at, '.');
    return dot && dot > at + 1 && *(dot + 1) != '\0';
}

/** Derive a PBKDF2-HMAC-SHA256 key from *password* and *salt*. */
static int derive_key(const char *password, const unsigned char *salt,
                      unsigned char *out) {
    return PKCS5_PBKDF2_HMAC(
        password, (int)strlen(password),
        salt, SALT_LEN,
        PBKDF2_ITER, EVP_sha256(),
        HASH_LEN, out
    );
}

/** Constant-time byte-array comparison — resists timing side-channels. */
static int ct_equal(const unsigned char *a, const unsigned char *b, size_t n) {
    unsigned char diff = 0;
    for (size_t i = 0; i < n; i++) diff |= a[i] ^ b[i];
    return diff == 0;
}

static User *find_user(const char *username) {
    for (int i = 0; i < MAX_USERS; i++)
        if (store[i].active && strcmp(store[i].username, username) == 0)
            return &store[i];
    return NULL;
}

/** Generic failure sentinel — no internal detail revealed to caller. */
static int auth_fail(const char **msg) {
    *msg = "Authentication failed. Please check your credentials.";
    return 0;
}

/* -------------------------------------------------------------------------
 * Public API
 * ---------------------------------------------------------------------- */

/**
 * register_user — create a new account.
 * Returns 1 on success, 0 on invalid input or duplicate username.
 */
int register_user(const char *username, const char *email, const char *password) {
    if (!username || strlen(username) == 0 || strlen(username) >= MAX_STR) return 0;
    if (!is_valid_email(email)) return 0;
    if (!password || strlen(password) == 0) return 0;
    if (find_user(username)) return 0;  /* duplicate */

    for (int i = 0; i < MAX_USERS; i++) {
        if (!store[i].active) {
            User *u = &store[i];
            if (!RAND_bytes(u->salt, SALT_LEN)) return 0;
            if (!derive_key(password, u->salt, u->password_hash)) return 0;
            strncpy(u->username, username, MAX_STR - 1);
            u->username[MAX_STR - 1] = '\0';
            strncpy(u->email, email, MAX_STR - 1);
            u->email[MAX_STR - 1] = '\0';
            u->authenticated = 0;
            u->active = 1;
            return 1;
        }
    }
    return 0;
}

/**
 * login_user — authenticate and start a session.
 * Returns the same value (0) for unknown user and wrong password to
 * prevent user enumeration.
 */
int login_user(const char *username, const char *password) {
    if (!username || !password) return 0;
    User *u = find_user(username);
    if (!u) return 0;
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash)) return 0;
    if (!ct_equal(hash, u->password_hash, HASH_LEN)) return 0;
    u->authenticated = 1;
    return 1;
}

/** logout_user — revoke the current session. */
void logout_user(const char *username) {
    User *u = find_user(username);
    if (u) u->authenticated = 0;
}

/**
 * change_email — update the authenticated user's email address.
 *
 * Steps:
 *  1. Validate all inputs (presence + email format).
 *  2. Confirm user exists and is authenticated.
 *  3. Verify the supplied password via PBKDF2 hash + constant-time compare.
 *  4. Verify the old_email matches the stored email.
 *  5. Perform the update only if all checks pass.
 *
 * Returns 1 on success, 0 on failure. *msg is always set to a safe string.
 */
int change_email(const char *username, const char *old_email,
                 const char *new_email, const char *password,
                 const char **msg) {

    /* Step 1 — input validation */
    if (!username || !old_email || !new_email || !password ||
            !*username || !*old_email || !*new_email || !*password) {
        *msg = "All fields are required.";
        return 0;
    }
    if (!is_valid_email(new_email)) {
        *msg = "Invalid email address format.";
        return 0;
    }
    if (strcmp(old_email, new_email) == 0) {
        *msg = "New email must differ from the current email.";
        return 0;
    }

    /* Step 2 — authentication check */
    User *u = find_user(username);
    if (!u || !u->authenticated) return auth_fail(msg);

    /* Step 3 — password verification */
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash)) return auth_fail(msg);
    if (!ct_equal(hash, u->password_hash, HASH_LEN)) return auth_fail(msg);

    /* Step 4 — old email verification */
    if (strcmp(u->email, old_email) != 0) return auth_fail(msg);

    /* Step 5 — perform the update */
    strncpy(u->email, new_email, MAX_STR - 1);
    u->email[MAX_STR - 1] = '\0';
    *msg = "Email updated successfully.";
    return 1;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    const char *msg;

    register_user("diana", "diana@example.com", "H@rdT0Guess!");
    login_user("diana", "H@rdT0Guess!");

    /* Successful change */
    printf("[%s] %s\n",
        change_email("diana", "diana@example.com", "diana.new@example.com", "H@rdT0Guess!", &msg)
            ? "PASS" : "FAIL", msg);

    /* Wrong password */
    printf("[%s] %s\n",
        change_email("diana", "diana.new@example.com", "x@x.com", "wrongpass", &msg)
            ? "PASS" : "FAIL expected", msg);

    /* Wrong old email */
    printf("[%s] %s\n",
        change_email("diana", "bad@example.com", "x@x.com", "H@rdT0Guess!", &msg)
            ? "PASS" : "FAIL expected", msg);

    /* Not logged in */
    logout_user("diana");
    printf("[%s] %s\n",
        change_email("diana", "diana.new@example.com", "x@x.com", "H@rdT0Guess!", &msg)
            ? "PASS" : "FAIL expected", msg);

    return 0;
}
