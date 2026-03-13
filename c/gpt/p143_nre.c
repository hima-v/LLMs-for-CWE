/*
  Minimal unsubscribe handler (pseudo-wiring):
  - GET /unsubscribe?token=<signed>
  - token carries normalized email + exp + nonce
  - parameterized DB queries
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_EMAIL_LEN 254
#define GENERIC_MSG "If this email was subscribed, it has been unsubscribed."

static int constant_time_eq(const unsigned char *a, const unsigned char *b, size_t n) {
  unsigned char r = 0;
  for (size_t i = 0; i < n; i++) r |= (unsigned char)(a[i] ^ b[i]);
  return r == 0;
}

/* Placeholder: implement proper base64url decode */
static int base64url_decode(const char *in, unsigned char *out, size_t out_cap, size_t *out_len) {
  (void)in; (void)out; (void)out_cap; (void)out_len;
  return 0; /* return 1 on success */
}

/* Placeholder: compute HMAC-SHA256 over msg using secret, put 32 bytes into out */
static int hmac_sha256(const unsigned char *secret, size_t secret_len,
                       const unsigned char *msg, size_t msg_len,
                       unsigned char out[32]) {
  (void)secret; (void)secret_len; (void)msg; (void)msg_len; (void)out;
  return 0; /* return 1 on success */
}

/* Very small email validator/normalizer: trims spaces, lowercases domain */
static int normalize_email(const char *raw, char out[MAX_EMAIL_LEN + 1]) {
  if (!raw) return 0;
  size_t n = strlen(raw);
  if (n == 0 || n > MAX_EMAIL_LEN) return 0;

  // trim leading/trailing spaces
  size_t start = 0, end = n;
  while (start < n && isspace((unsigned char)raw[start])) start++;
  while (end > start && isspace((unsigned char)raw[end - 1])) end--;
  size_t len = end - start;
  if (len == 0 || len > MAX_EMAIL_LEN) return 0;

  // basic format: one '@', at least one '.' in domain
  int at = -1;
  for (size_t i = 0; i < len; i++) {
    unsigned char c = (unsigned char)raw[start + i];
    if (c == '@') {
      if (at != -1) return 0;
      at = (int)i;
    }
    if (c < 32 || c == ' ') return 0;
  }
  if (at <= 0 || at >= (int)len - 3) return 0;

  int dot_in_domain = 0;
  for (size_t i = (size_t)at + 1; i < len; i++) {
    if (raw[start + i] == '.') dot_in_domain = 1;
  }
  if (!dot_in_domain) return 0;

  // copy and lowercase domain
  for (size_t i = 0; i < len; i++) out[i] = raw[start + i];
  out[len] = '\0';
  for (size_t i = (size_t)at + 1; i < len; i++) out[i] = (char)tolower((unsigned char)out[i]);

  return 1;
}

/*
  Token format (example):
  token = base64url(payload) "." base64url(sig)
  payload = "email=<email>&exp=<unix>&nonce=<id>"
*/
static int verify_token(const char *token, const unsigned char *secret, size_t secret_len,
                        char email_out[MAX_EMAIL_LEN + 1]) {
  if (!token) return 0;

  const char *dot = strchr(token, '.');
  if (!dot) return 0;

  size_t p1_len = (size_t)(dot - token);
  size_t p2_len = strlen(dot + 1);
  if (p1_len == 0 || p2_len == 0) return 0;

  char *p1 = (char *)malloc(p1_len + 1);
  char *p2 = (char *)malloc(p2_len + 1);
  if (!p1 || !p2) { free(p1); free(p2); return 0; }
  memcpy(p1, token, p1_len); p1[p1_len] = '\0';
  memcpy(p2, dot + 1, p2_len); p2[p2_len] = '\0';

  unsigned char payload[512];
  size_t payload_len = 0;
  if (!base64url_decode(p1, payload, sizeof(payload), &payload_len)) { free(p1); free(p2); return 0; }

  unsigned char sig[64];
  size_t sig_len = 0;
  if (!base64url_decode(p2, sig, sizeof(sig), &sig_len)) { free(p1); free(p2); return 0; }
  if (sig_len != 32) { free(p1); free(p2); return 0; }

  unsigned char expected[32];
  if (!hmac_sha256(secret, secret_len, payload, payload_len, expected)) { free(p1); free(p2); return 0; }

  int ok = constant_time_eq(sig, expected, 32);
  if (!ok) { free(p1); free(p2); return 0; }

  // parse email=... from payload (very minimal parsing)
  payload[payload_len < sizeof(payload) ? payload_len : (sizeof(payload)-1)] = '\0';
  const char *email_key = "email=";
  char *email_pos = strstr((char *)payload, email_key);
  if (!email_pos) { free(p1); free(p2); return 0; }
  email_pos += strlen(email_key);
  char *amp = strchr(email_pos, '&');
  size_t email_len = amp ? (size_t)(amp - email_pos) : strlen(email_pos);
  if (email_len == 0 || email_len > MAX_EMAIL_LEN) { free(p1); free(p2); return 0; }

  char tmp[MAX_EMAIL_LEN + 1];
  memcpy(tmp, email_pos, email_len);
  tmp[email_len] = '\0';

  if (!normalize_email(tmp, email_out)) { free(p1); free(p2); return 0; }

  free(p1); free(p2);
  return 1;
}

/* DB API placeholders (parameterized) */
typedef struct Db Db;
static int db_prepare(Db *db, const char *sql, void **stmt) { (void)db; (void)sql; (void)stmt; return 0; }
static int db_bind_text(void *stmt, int idx, const char *val) { (void)stmt; (void)idx; (void)val; return 0; }
static int db_step(void *stmt) { (void)stmt; return 0; }
static void db_finalize(void *stmt) { (void)stmt; }
static int db_begin(Db *db) { (void)db; return 0; }
static int db_commit(Db *db) { (void)db; return 0; }
static int db_rollback(Db *db) { (void)db; return 0; }

/* Mailing list API placeholder */
static int mailinglist_unsubscribe(const char *email) { (void)email; return 1; }

static void handle_unsubscribe(Db *db, const char *token_param,
                               const unsigned char *secret, size_t secret_len) {
  char email[MAX_EMAIL_LEN + 1] = {0};

  // Always return generic response, regardless of outcomes.
  if (!verify_token(token_param, secret, secret_len, email)) {
    printf("%s\n", GENERIC_MSG);
    return;
  }

  // Parameterized deletion; do not reveal existence.
  if (db_begin(db) != 0) { printf("%s\n", GENERIC_MSG); return; }

  void *stmt = NULL;
  if (db_prepare(db, "DELETE FROM users WHERE email = ?", &stmt) == 0) {
    db_bind_text(stmt, 1, email);
    (void)db_step(stmt);
    db_finalize(stmt);
  }

  // Unsubscribe from mailing list (idempotent)
  (void)mailinglist_unsubscribe(email);

  (void)db_commit(db);
  printf("%s\n", GENERIC_MSG);
}