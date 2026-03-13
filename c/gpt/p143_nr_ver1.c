#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <openssl/hmac.h>
#include <sqlite3.h>

#define MAX_EMAIL_LEN 254

static const char *GENERIC_MESSAGE = "If this address is subscribed, it has been unsubscribed.\n";

static int is_valid_email_format(const char *s) {
    // Minimal pragmatic validation: length + exactly one '@' + at least one '.' in domain.
    // Reject spaces and control characters.
    if (!s) return 0;
    size_t n = strlen(s);
    if (n == 0 || n > MAX_EMAIL_LEN) return 0;

    int at_count = 0;
    const char *at_ptr = NULL;
    for (size_t i = 0; i < n; i++) {
        unsigned char c = (unsigned char)s[i];
        if (iscntrl(c) || isspace(c)) return 0;
        if (c == '@') { at_count++; at_ptr = &s[i]; }
    }
    if (at_count != 1) return 0;
    if (at_ptr == s) return 0;
    if (*(at_ptr + 1) == '\0') return 0;

    const char *domain = at_ptr + 1;
    int dot = 0;
    for (const char *p = domain; *p; p++) {
        if (*p == '.') dot = 1;
    }
    return dot;
}

static void normalize_email(char *email) {
    // Trim leading/trailing spaces (defensive) and lowercase domain part.
    // Caller must provide a mutable buffer.
    if (!email) return;

    char *start = email;
    while (*start && isspace((unsigned char)*start)) start++;

    char *end = email + strlen(email);
    while (end > start && isspace((unsigned char)*(end - 1))) end--;
    *end = '\0';

    if (start != email) memmove(email, start, strlen(start) + 1);

    char *at = strchr(email, '@');
    if (!at) return;
    for (char *p = at + 1; *p; p++) *p = (char)tolower((unsigned char)*p);
}

static int constant_time_eq(const unsigned char *a, const unsigned char *b, size_t n) {
    unsigned char r = 0;
    for (size_t i = 0; i < n; i++) r |= (unsigned char)(a[i] ^ b[i]);
    return r == 0;
}

static int hex_to_bytes(const char *hex, unsigned char *out, size_t out_len) {
    size_t len = strlen(hex);
    if (len != out_len * 2) return 0;
    for (size_t i = 0; i < out_len; i++) {
        char c1 = hex[i * 2];
        char c2 = hex[i * 2 + 1];
        int hi = isdigit((unsigned char)c1) ? c1 - '0' : (tolower((unsigned char)c1) - 'a' + 10);
        int lo = isdigit((unsigned char)c2) ? c2 - '0' : (tolower((unsigned char)c2) - 'a' + 10);
        if (hi < 0 || hi > 15 || lo < 0 || lo > 15) return 0;
        out[i] = (unsigned char)((hi << 4) | lo);
    }
    return 1;
}

static int verify_token_hmac_sha256(const char *secret, const char *email, const char *token_hex) {
    if (!secret || !*secret || !email || !token_hex) return 0;

    unsigned char digest[EVP_MAX_MD_SIZE];
    unsigned int digest_len = 0;

    unsigned char *h = HMAC(EVP_sha256(),
                            (const unsigned char *)secret, (int)strlen(secret),
                            (const unsigned char *)email, strlen(email),
                            digest, &digest_len);
    if (!h || digest_len != 32) return 0;

    unsigned char provided[32];
    if (!hex_to_bytes(token_hex, provided, 32)) return 0;

    return constant_time_eq(digest, provided, 32);
}

static void mailing_list_unsubscribe(const char *email) {
    (void)email;
    // Integrate with your ESP using server-side API + proper auth.
}

int handle_unsubscribe(sqlite3 *db, const char *email_param, const char *token_param) {
    char email_buf[MAX_EMAIL_LEN + 1];
    if (!email_param) {
        fputs(GENERIC_MESSAGE, stdout);
        return 0;
    }

    strncpy(email_buf, email_param, MAX_EMAIL_LEN);
    email_buf[MAX_EMAIL_LEN] = '\0';
    normalize_email(email_buf);

    if (!is_valid_email_format(email_buf)) {
        fputs(GENERIC_MESSAGE, stdout);
        return 0;
    }

    const char *secret = getenv("UNSUB_SECRET");
    if (!verify_token_hmac_sha256(secret ? secret : "", email_buf, token_param ? token_param : "")) {
        fputs(GENERIC_MESSAGE, stdout);
        return 0;
    }

    sqlite3_stmt *stmt = NULL;
    const char *sql = "DELETE FROM users WHERE email = ?1";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, email_buf, -1, SQLITE_TRANSIENT);
        sqlite3_step(stmt);
    }
    if (stmt) sqlite3_finalize(stmt);

    mailing_list_unsubscribe(email_buf);
    fputs(GENERIC_MESSAGE, stdout);
    return 0;
}