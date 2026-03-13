/* C (CivetWeb + SQLite + OpenSSL) — GET /unsubscribe?email=...&token=...
   Build: gcc unsub.c -o unsub -lcivetweb -lsqlite3 -lssl -lcrypto
*/
#include "civetweb.h"
#include <sqlite3.h>
#include <openssl/hmac.h>
#include <openssl/evp.h>
#include <openssl/sha.h>
#include <openssl/crypto.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define EMAIL_MAX_LEN 254
#define GENERIC_JSON "{\"message\":\"If this email was subscribed, it has been unsubscribed.\"}"

static const unsigned char HMAC_SECRET[] = "replace-with-strong-secret";
static const char *DB_PATH = "app.db";

static int normalize_email(const char *raw, char out[EMAIL_MAX_LEN+1]) {
  if (!raw) return 0;
  size_t n = strlen(raw);

  while (n && isspace((unsigned char)raw[0])) { raw++; n--; }
  while (n && isspace((unsigned char)raw[n-1])) n--;

  if (n == 0 || n > EMAIL_MAX_LEN) return 0;

  int at = -1;
  for (size_t i=0;i<n;i++) {
    unsigned char c = (unsigned char)raw[i];
    if (!(isalnum(c) || c=='.' || c=='_' || c=='%' || c=='+' || c=='-' || c=='@')) return 0;
    if (c=='@') { if (at != -1) return 0; at = (int)i; }
    out[i] = (char)tolower(c);
  }
  out[n] = '\0';
  if (at <= 0 || at >= (int)n-3) return 0;
  const char *dot = strchr(out + at + 1, '.');
  if (!dot || dot == out + n - 1) return 0;
  return 1;
}

static int b64url_decode(const char *in, unsigned char **out, size_t *outlen) {
  if (!in) return 0;
  size_t len = strlen(in);
  if (len == 0) return 0;

  char *tmp = (char*)malloc(len + 5);
  if (!tmp) return 0;
  strcpy(tmp, in);
  for (size_t i=0;i<len;i++) {
    if (tmp[i] == '-') tmp[i] = '+';
    else if (tmp[i] == '_') tmp[i] = '/';
  }
  size_t pad = (4 - (len % 4)) % 4;
  for (size_t i=0;i<pad;i++) tmp[len+i] = '=';
  tmp[len+pad] = '\0';

  size_t alloc = (len + pad) * 3 / 4 + 4;
  unsigned char *buf = (unsigned char*)malloc(alloc);
  if (!buf) { free(tmp); return 0; }

  int dec = EVP_DecodeBlock(buf, (unsigned char*)tmp, (int)(len+pad));
  free(tmp);
  if (dec < 0) { free(buf); return 0; }

  *out = buf;
  *outlen = (size_t)dec;
  return 1;
}

static void sha256_hex(const char *s, char hex[65]) {
  unsigned char dig[SHA256_DIGEST_LENGTH];
  SHA256((const unsigned char*)s, strlen(s), dig);
  for (int i=0;i<SHA256_DIGEST_LENGTH;i++) sprintf(hex + (i*2), "%02x", dig[i]);
  hex[64] = '\0';
}

static int verify_signed_token(const char *email_norm, const char *token) {
  if (!email_norm || !token) return 0;
  const char *dot = strchr(token, '.');
  if (!dot) return 0;

  size_t email_b64_len = (size_t)(dot - token);
  if (email_b64_len == 0) return 0;

  char *email_b64 = (char*)malloc(email_b64_len + 1);
  if (!email_b64) return 0;
  memcpy(email_b64, token, email_b64_len);
  email_b64[email_b64_len] = '\0';

  const char *sig_b64 = dot + 1;
  if (*sig_b64 == '\0') { free(email_b64); return 0; }

  unsigned char *email_dec = NULL, *sig_dec = NULL;
  size_t email_dec_len = 0, sig_dec_len = 0;

  if (!b64url_decode(email_b64, &email_dec, &email_dec_len) ||
      !b64url_decode(sig_b64, &sig_dec, &sig_dec_len)) {
    free(email_b64);
    free(email_dec);
    free(sig_dec);
    return 0;
  }

  int ok = 0;
  if (email_dec_len <= EMAIL_MAX_LEN) {
    char token_email[EMAIL_MAX_LEN+1];
    memcpy(token_email, email_dec, email_dec_len);
    token_email[email_dec_len] = '\0';

    if (strcmp(token_email, email_norm) == 0) {
      unsigned int mac_len = 0;
      unsigned char mac[EVP_MAX_MD_SIZE];
      HMAC(EVP_sha256(), HMAC_SECRET, (int)strlen((const char*)HMAC_SECRET),
           (const unsigned char*)email_b64, (int)strlen(email_b64), mac, &mac_len);

      if (mac_len == sig_dec_len && CRYPTO_memcmp(mac, sig_dec, mac_len) == 0) ok = 1;
    }
  }

  free(email_b64);
  free(email_dec);
  free(sig_dec);
  return ok;
}

static int send_json(struct mg_connection *conn, const char *json) {
  mg_printf(conn,
            "HTTP/1.1 200 OK\r\n"
            "Content-Type: application/json\r\n"
            "Content-Length: %zu\r\n"
            "\r\n"
            "%s",
            strlen(json), json);
  return 200;
}

static void handle_unsubscribe(sqlite3 *db, const char *email_norm, const char *token) {
  sqlite3_exec(db, "BEGIN IMMEDIATE;", NULL, NULL, NULL);

  long now = (long)time(NULL);
  char token_hash[65];
  sha256_hex(token, token_hash);

  sqlite3_stmt *sel = NULL;
  const char *sel_sql =
    "SELECT used_at, expires_at FROM unsubscribe_tokens WHERE token_hash=? AND email=?;";
  if (sqlite3_prepare_v2(db, sel_sql, -1, &sel, NULL) == SQLITE_OK) {
    sqlite3_bind_text(sel, 1, token_hash, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(sel, 2, email_norm, -1, SQLITE_TRANSIENT);

    if (sqlite3_step(sel) == SQLITE_ROW) {
      int used_null = (sqlite3_column_type(sel, 0) == SQLITE_NULL);
      int exp_null  = (sqlite3_column_type(sel, 1) == SQLITE_NULL);
      long exp = exp_null ? 0 : sqlite3_column_int64(sel, 1);

      if (used_null && (exp_null || exp >= now)) {
        sqlite3_stmt *upd = NULL;
        const char *upd_sql =
          "UPDATE unsubscribe_tokens SET used_at=? WHERE token_hash=? AND email=? AND used_at IS NULL;";
        if (sqlite3_prepare_v2(db, upd_sql, -1, &upd, NULL) == SQLITE_OK) {
          sqlite3_bind_int64(upd, 1, now);
          sqlite3_bind_text(upd, 2, token_hash, -1, SQLITE_TRANSIENT);
          sqlite3_bind_text(upd, 3, email_norm, -1, SQLITE_TRANSIENT);
          sqlite3_step(upd);
        }
        sqlite3_finalize(upd);
      }
    }
  }
  sqlite3_finalize(sel);

  sqlite3_stmt *del = NULL;
  const char *del_sql = "DELETE FROM users WHERE email=?;";
  if (sqlite3_prepare_v2(db, del_sql, -1, &del, NULL) == SQLITE_OK) {
    sqlite3_bind_text(del, 1, email_norm, -1, SQLITE_TRANSIENT);
    sqlite3_step(del);
  }
  sqlite3_finalize(del);

  sqlite3_exec(db, "COMMIT;", NULL, NULL, NULL);
}

static int handler(struct mg_connection *conn, void *cbdata) {
  (void)cbdata;

  const struct mg_request_info *ri = mg_get_request_info(conn);
  if (strcmp(ri->request_method, "GET") != 0) return send_json(conn, GENERIC_JSON);
  if (strcmp(ri->local_uri, "/unsubscribe") != 0) return 404;

  char email_q[512] = {0}, token_q[2048] = {0};
  const char *email = NULL;
  const char *token = NULL;

  mg_get_var(ri->query_string ? ri->query_string : "", strlen(ri->query_string ? ri->query_string : ""),
             "email", email_q, sizeof(email_q));
  mg_get_var(ri->query_string ? ri->query_string : "", strlen(ri->query_string ? ri->query_string : ""),
             "token", token_q, sizeof(token_q));

  email = email_q[0] ? email_q : NULL;
  token = token_q[0] ? token_q : NULL;

  char email_norm[EMAIL_MAX_LEN+1];
  if (!normalize_email(email, email_norm) || !verify_signed_token(email_norm, token)) {
    return send_json(conn, GENERIC_JSON);
  }

  sqlite3 *db = NULL;
  if (sqlite3_open(DB_PATH, &db) == SQLITE_OK) {
    handle_unsubscribe(db, email_norm, token);
    sqlite3_close(db);
  }

  return send_json(conn, GENERIC_JSON);
}

int main(void) {
  const char *options[] = {"listening_ports", "8080", 0};
  struct mg_callbacks callbacks;
  memset(&callbacks, 0, sizeof(callbacks));

  struct mg_context *ctx = mg_start(&callbacks, 0, options);
  if (!ctx) return 1;

  mg_set_request_handler(ctx, "/unsubscribe", handler, 0);

  getchar();

  mg_stop(ctx);
  return 0;
}