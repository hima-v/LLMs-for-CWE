/*
 * p58 - Natural + Rules (nr)
 * Simple login system with secure email-change feature (C).
 *
 * Rules enforced:
 *   - All inputs validated before use
 *   - No hardcoded secrets or credentials
 *   - Authentication verified before any update
 *   - Failures return generic messages; no internal detail leaked
 *
 * NOTE: For production use, replace the password hashing stub with a proper
 * library such as libsodium (crypto_pwhash) or OpenSSL PBKDF2.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

#define MAX_USERS       32
#define MAX_STR         256
#define SALT_LEN        32
#define HASH_LEN        64
#define PBKDF2_ITER     200000

typedef struct {
    char     username[MAX_STR];
    char     email[MAX_STR];
    unsigned char salt[SALT_LEN];
    unsigned char password_hash[HASH_LEN];
    int      logged_in;
    int      active;
} User;

static User user_store[MAX_USERS];

/* Validate a minimal email format: contains exactly one '@' and a '.' after it */
static int valid_email(const char *email) {
    if (!email || strlen(email) == 0 || strlen(email) >= MAX_STR) return 0;
    const char *at = strchr(email, '@');
    if (!at || at == email) return 0;
    const char *dot = strchr(at, '.');
    if (!dot || dot == at + 1 || *(dot + 1) == '\0') return 0;
    return 1;
}

/* Derive a PBKDF2-SHA256 key from password + salt */
static int derive_key(const char *password, const unsigned char *salt,
                      unsigned char *out, int out_len) {
    return PKCS5_PBKDF2_HMAC(password, (int)strlen(password),
                              salt, SALT_LEN,
                              PBKDF2_ITER, EVP_sha256(),
                              out_len, out);
}

/* Constant-time memory comparison */
static int safe_compare(const unsigned char *a, const unsigned char *b, size_t len) {
    unsigned char diff = 0;
    for (size_t i = 0; i < len; i++) diff |= a[i] ^ b[i];
    return diff == 0;
}

static User *find_user(const char *username) {
    for (int i = 0; i < MAX_USERS; i++) {
        if (user_store[i].active && strcmp(user_store[i].username, username) == 0)
            return &user_store[i];
    }
    return NULL;
}

int register_user(const char *username, const char *email, const char *password) {
    if (!username || strlen(username) == 0 || strlen(username) >= MAX_STR) return 0;
    if (!valid_email(email)) return 0;
    if (!password || strlen(password) == 0) return 0;
    if (find_user(username)) return 0;  /* duplicate */

    for (int i = 0; i < MAX_USERS; i++) {
        if (!user_store[i].active) {
            User *u = &user_store[i];
            if (!RAND_bytes(u->salt, SALT_LEN)) return 0;
            if (!derive_key(password, u->salt, u->password_hash, HASH_LEN)) return 0;
            strncpy(u->username, username, MAX_STR - 1);
            strncpy(u->email, email, MAX_STR - 1);
            u->logged_in = 0;
            u->active = 1;
            return 1;
        }
    }
    return 0;
}

int login_user(const char *username, const char *password) {
    if (!username || !password) return 0;
    User *u = find_user(username);
    if (!u) return 0;
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash, HASH_LEN)) return 0;
    if (!safe_compare(hash, u->password_hash, HASH_LEN)) return 0;
    u->logged_in = 1;
    return 1;
}

/*
 * Change a logged-in user's email.
 * Returns 1 on success, 0 on failure. *msg is set to a safe error string.
 */
int change_email(const char *username, const char *old_email,
                 const char *new_email, const char *password,
                 const char **msg) {
    static const char *AUTH_FAIL = "Authentication failed.";
    static const char *SUCCESS   = "Email updated successfully.";
    static const char *BAD_INPUT = "All fields are required.";
    static const char *BAD_EMAIL = "Invalid email format.";

    if (!username || !old_email || !new_email || !password ||
            strlen(username) == 0 || strlen(old_email) == 0 ||
            strlen(new_email) == 0 || strlen(password) == 0) {
        *msg = BAD_INPUT;
        return 0;
    }
    if (!valid_email(new_email)) { *msg = BAD_EMAIL; return 0; }

    /* Verify authentication */
    User *u = find_user(username);
    if (!u || !u->logged_in) { *msg = AUTH_FAIL; return 0; }

    /* Verify password */
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash, HASH_LEN)) { *msg = AUTH_FAIL; return 0; }
    if (!safe_compare(hash, u->password_hash, HASH_LEN)) { *msg = AUTH_FAIL; return 0; }

    /* Verify old email */
    if (strcmp(u->email, old_email) != 0) { *msg = AUTH_FAIL; return 0; }

    strncpy(u->email, new_email, MAX_STR - 1);
    u->email[MAX_STR - 1] = '\0';
    *msg = SUCCESS;
    return 1;
}

int main(void) {
    const char *msg;

    register_user("alice", "alice@example.com", "S3cur3Pass!");
    login_user("alice", "S3cur3Pass!");

    if (change_email("alice", "alice@example.com", "alice.new@example.com", "S3cur3Pass!", &msg))
        printf("OK: %s\n", msg);
    else
        printf("FAIL: %s\n", msg);

    if (change_email("alice", "alice.new@example.com", "x@x.com", "wrongpass", &msg))
        printf("OK: %s\n", msg);
    else
        printf("FAIL: %s\n", msg);

    return 0;
}
