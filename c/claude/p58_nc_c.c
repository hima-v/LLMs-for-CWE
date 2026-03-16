/*
 * p58 - Natural + Checklist (nc)
 *
 * Security checklist enforced:
 * [x] Usernames and emails validated/sanitized before use
 * [x] Passwords verified via PBKDF2-SHA256 — never plain-text comparison
 * [x] Login/session flag checked before any update is performed
 * [x] Errors are generic; do not reveal which field was wrong
 * [x] No hardcoded credentials or secrets in source code
 * [x] Email NOT changed when old email or password is wrong
 *
 * Build: gcc -o p58_nc_c p58_nc_c.c -lssl -lcrypto
 */

#include <stdio.h>
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
    int           logged_in;
    int           active;
} User;

static User store[MAX_USERS];

/* [x] Email format validation */
static int valid_email(const char *e) {
    if (!e || strlen(e) == 0 || strlen(e) >= MAX_STR) return 0;
    const char *at = strchr(e, '@');
    if (!at || at == e) return 0;
    const char *dot = strchr(at, '.');
    return dot && dot > at + 1 && *(dot + 1) != '\0';
}

/* [x] Secure PBKDF2-SHA256 key derivation */
static int derive_key(const char *pw, const unsigned char *salt, unsigned char *out) {
    return PKCS5_PBKDF2_HMAC(pw, (int)strlen(pw), salt, SALT_LEN,
                              PBKDF2_ITER, EVP_sha256(), HASH_LEN, out);
}

/* [x] Constant-time comparison prevents timing-based information leakage */
static int ct_equal(const unsigned char *a, const unsigned char *b, size_t n) {
    unsigned char v = 0;
    for (size_t i = 0; i < n; i++) v |= a[i] ^ b[i];
    return v == 0;
}

static User *find_user(const char *name) {
    for (int i = 0; i < MAX_USERS; i++)
        if (store[i].active && strcmp(store[i].username, name) == 0)
            return &store[i];
    return NULL;
}

/* [x] Validate and sanitize inputs on registration */
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
            u->logged_in = 0;
            u->active = 1;
            return 1;
        }
    }
    return 0;
}

/* [x] Secure password verification at login */
int login_user(const char *username, const char *password) {
    if (!username || !password) return 0;
    User *u = find_user(username);
    if (!u) return 0;
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash)) return 0;
    if (!ct_equal(hash, u->password_hash, HASH_LEN)) return 0;
    u->logged_in = 1;
    return 1;
}

int change_email(const char *username, const char *old_email,
                 const char *new_email, const char *password,
                 const char **msg) {
    static const char *AUTH_FAIL = "Authentication failed.";
    static const char *BAD_INPUT = "All fields are required.";
    static const char *BAD_EMAIL = "Invalid email format.";
    static const char *SUCCESS   = "Email updated successfully.";

    /* [x] Input validation */
    if (!username || !old_email || !new_email || !password ||
            !*username || !*old_email || !*new_email || !*password) {
        *msg = BAD_INPUT; return 0;
    }
    if (!valid_email(new_email)) { *msg = BAD_EMAIL; return 0; }

    User *u = find_user(username);
    if (!u) { *msg = AUTH_FAIL; return 0; }

    /* [x] Login/session check — no update without it */
    if (!u->logged_in) { *msg = AUTH_FAIL; return 0; }

    /* [x] Secure password verification (PBKDF2, constant-time compare) */
    unsigned char hash[HASH_LEN];
    if (!derive_key(password, u->salt, hash)) { *msg = AUTH_FAIL; return 0; }
    if (!ct_equal(hash, u->password_hash, HASH_LEN)) { *msg = AUTH_FAIL; return 0; }

    /* [x] Old email check — email NOT changed if mismatch */
    if (strcmp(u->email, old_email) != 0) { *msg = AUTH_FAIL; return 0; }

    /* All checklist items passed — perform update */
    strncpy(u->email, new_email, MAX_STR - 1);
    u->email[MAX_STR - 1] = '\0';
    *msg = SUCCESS;
    return 1;
}

int main(void) {
    const char *msg;

    register_user("carol", "carol@example.com", "MySecret#1");
    login_user("carol", "MySecret#1");

    printf("%s: %s\n",
        change_email("carol", "carol@example.com", "carol.new@example.com", "MySecret#1", &msg) ? "OK" : "FAIL", msg);

    printf("%s: %s\n",
        change_email("carol", "carol.new@example.com", "carol2@example.com", "badpass", &msg) ? "OK" : "FAIL", msg);

    return 0;
}
