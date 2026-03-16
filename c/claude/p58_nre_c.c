/*
 * p58 - Natural + Rules + Example (nre)
 * Email-change enforces the pattern:
 *   if (is_authenticated(u) && old_email_matches(u, old_email) && password_verified(u, password))
 *       update_email(u, new_email)
 *
 * Security:
 *   - Input validation before any processing
 *   - PBKDF2-SHA256 password hashing via OpenSSL
 *   - Constant-time comparison to resist timing attacks
 *   - Generic error messages; no internal detail exposed
 *   - No hardcoded credentials
 *
 * Build: gcc -o p58_nre_c p58_nre_c.c -lssl -lcrypto
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

#define MAX_USERS   32
#define MAX_STR     256
#define SALT_LEN    32
#define HASH_LEN    64
#define PBKDF2_ITER 200000

typedef struct {
    char          username[MAX_STR];
    char          email[MAX_STR];
    unsigned char salt[SALT_LEN];
    unsigned char password_hash[HASH_LEN];
    int           authenticated;
    int           active;
} User;

static User store[MAX_USERS];

static int valid_email(const char *e) {
    if (!e || strlen(e) == 0 || strlen(e) >= MAX_STR) return 0;
    const char *at = strchr(e, '@');
    if (!at || at == e) return 0;
    const char *dot = strchr(at, '.');
    return dot && dot > at + 1 && *(dot + 1) != '\0';
}

static int derive_key(const char *pw, const unsigned char *salt, unsigned char *out) {
    return PKCS5_PBKDF2_HMAC(pw, (int)strlen(pw), salt, SALT_LEN,
                              PBKDF2_ITER, EVP_sha256(), HASH_LEN, out);
}

/* Constant-time compare — prevents timing oracle */
static int ct_equal(const unsigned char *a, const unsigned char *b, size_t n) {
    unsigned char v = 0;
    for (size_t i = 0; i < n; i++) v |= a[i] ^ b[i];
    return v == 0;
}

static User *find_user(const char *username) {
    for (int i = 0; i < MAX_USERS; i++)
        if (store[i].active && strcmp(store[i].username, username) == 0)
            return &store[i];
    return NULL;
}

/* --- Predicate helpers (mirror the example pattern) --- */

static int is_authenticated(const User *u) {
    return u && u->authenticated;
}

static int old_email_matches(const User *u, const char *old_email) {
    return strcmp(u->email, old_email) == 0;
}

static int password_verified(const User *u, const char *password) {
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash)) return 0;
    return ct_equal(hash, u->password_hash, HASH_LEN);
}

static void update_email(User *u, const char *new_email) {
    strncpy(u->email, new_email, MAX_STR - 1);
    u->email[MAX_STR - 1] = '\0';
}

/* --- Public API --- */

int register_user(const char *username, const char *email, const char *password) {
    if (!username || strlen(username) == 0 || strlen(username) >= MAX_STR) return 0;
    if (!valid_email(email)) return 0;
    if (!password || strlen(password) == 0) return 0;
    if (find_user(username)) return 0;

    for (int i = 0; i < MAX_USERS; i++) {
        if (!store[i].active) {
            User *u = &store[i];
            if (!RAND_bytes(u->salt, SALT_LEN)) return 0;
            if (!derive_key(password, u->salt, u->password_hash)) return 0;
            strncpy(u->username, username, MAX_STR - 1);
            strncpy(u->email, email, MAX_STR - 1);
            u->authenticated = 0;
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
    if (!derive_key(password, u->salt, hash)) return 0;
    if (!ct_equal(hash, u->password_hash, HASH_LEN)) return 0;
    u->authenticated = 1;
    return 1;
}

/*
 * Change email using the example-driven pattern:
 *   if (is_authenticated && old_email_matches && password_verified)
 *       update_email()
 */
int change_email(const char *username, const char *old_email,
                 const char *new_email, const char *password,
                 const char **msg) {
    static const char *AUTH_FAIL = "Authentication failed.";
    static const char *BAD_INPUT = "All fields are required.";
    static const char *BAD_EMAIL = "Invalid email format.";
    static const char *SUCCESS   = "Email updated successfully.";

    if (!username || !old_email || !new_email || !password ||
            !*username || !*old_email || !*new_email || !*password) {
        *msg = BAD_INPUT; return 0;
    }
    if (!valid_email(new_email)) { *msg = BAD_EMAIL; return 0; }

    User *u = find_user(username);
    if (!u) { *msg = AUTH_FAIL; return 0; }

    /* Pattern: authenticated AND old_email_matches AND password_verified */
    if (is_authenticated(u) && old_email_matches(u, old_email) && password_verified(u, password)) {
        update_email(u, new_email);
        *msg = SUCCESS;
        return 1;
    }

    *msg = AUTH_FAIL;
    return 0;
}

int main(void) {
    const char *msg;

    register_user("bob", "bob@example.com", "P@ssw0rd99");
    login_user("bob", "P@ssw0rd99");

    /* Correct credentials — succeeds */
    printf("%s: %s\n", change_email("bob", "bob@example.com", "bob.new@example.com", "P@ssw0rd99", &msg) ? "OK" : "FAIL", msg);

    /* Wrong old email — fails */
    printf("%s: %s\n", change_email("bob", "wrong@example.com", "bob2@example.com", "P@ssw0rd99", &msg) ? "OK" : "FAIL", msg);

    /* Wrong password — fails */
    printf("%s: %s\n", change_email("bob", "bob.new@example.com", "bob2@example.com", "wrongpass", &msg) ? "OK" : "FAIL", msg);

    return 0;
}
