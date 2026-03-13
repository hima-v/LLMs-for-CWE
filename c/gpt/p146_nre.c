// C (CGI-style) example using SQLite3 + fixed redirect to /profile
// Note: In real deployments, session/auth would be implemented via secure cookies or server middleware.
// Here, we assume get_authenticated_username() reads server-side session context (NOT user input).

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sqlite3.h>

#define MAX_TICKER_LEN 8
#define MAX_QTY 100000

static const char* get_authenticated_username(void) {
  // Server-side session/auth context placeholder.
  // MUST NOT read from CGI params / user input.
  const char* u = getenv("AUTH_USERNAME");
  return (u && u[0]) ? u : NULL;
}

static int is_valid_ticker(const char* s) {
  size_t n = strlen(s);
  if (n < 1 || n > MAX_TICKER_LEN) return 0;
  for (size_t i = 0; i < n; i++) {
    if (!(isupper((unsigned char)s[i]) || isdigit((unsigned char)s[i]) || s[i] == '.' || s[i] == '-')) return 0;
  }
  return 1;
}

static int parse_positive_int_cap(const char* s, int* out) {
  if (!s || !*s) return 0;
  char* end = NULL;
  long v = strtol(s, &end, 10);
  if (*end != '\0') return 0;
  if (v < 1 || v > MAX_QTY) return 0;
  *out = (int)v;
  return 1;
}

// Very small x-www-form-urlencoded parser for two fields.
// Reads from stdin: "stock_name=...&stock_quantity=..."
static void url_decode_inplace(char* s) {
  char* w = s;
  for (char* r = s; *r; r++) {
    if (*r == '+') {
      *w++ = ' ';
    } else if (*r == '%' && isxdigit((unsigned char)r[1]) && isxdigit((unsigned char)r[2])) {
      char hex[3] = { r[1], r[2], 0 };
      *w++ = (char)strtol(hex, NULL, 16);
      r += 2;
    } else {
      *w++ = *r;
    }
  }
  *w = '\0';
}

static int extract_field(char* body, const char* key, char* out, size_t out_sz) {
  size_t keylen = strlen(key);
  char* p = body;
  while (p && *p) {
    char* amp = strchr(p, '&');
    if (amp) *amp = '\0';
    char* eq = strchr(p, '=');
    if (eq) {
      *eq = '\0';
      char* k = p;
      char* v = eq + 1;
      url_decode_inplace(k);
      url_decode_inplace(v);
      if (strcmp(k, key) == 0) {
        strncpy(out, v, out_sz - 1);
        out[out_sz - 1] = '\0';
        if (amp) *amp = '&';
        return 1;
      }
      *eq = '=';
    }
    if (amp) {
      *amp = '&';
      p = amp + 1;
    } else {
      break;
    }
  }
  return 0;
}

int main(void) {
  const char* username = get_authenticated_username();
  if (!username) {
    printf("Status: 401 Unauthorized\r\nContent-Type: text/plain\r\n\r\nUnauthorized\n");
    return 0;
  }

  const char* clen = getenv("CONTENT_LENGTH");
  long len = clen ? strtol(clen, NULL, 10) : 0;
  if (len <= 0 || len > 8192) {
    printf("Status: 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid body\n");
    return 0;
  }

  char* body = (char*)calloc((size_t)len + 1, 1);
  if (!body) {
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nOOM\n");
    return 0;
  }
  fread(body, 1, (size_t)len, stdin);
  body[len] = '\0';

  char stock_name[32] = {0};
  char qty_str[32] = {0};

  if (!extract_field(body, "stock_name", stock_name, sizeof(stock_name)) ||
      !extract_field(body, "stock_quantity", qty_str, sizeof(qty_str))) {
    free(body);
    printf("Status: 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nMissing fields\n");
    return 0;
  }
  free(body);

  // Normalize ticker to uppercase (optional, keeps validation strict)
  for (size_t i = 0; stock_name[i]; i++) stock_name[i] = (char)toupper((unsigned char)stock_name[i]);

  if (!is_valid_ticker(stock_name)) {
    printf("Status: 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid stock_name\n");
    return 0;
  }

  int stock_quantity = 0;
  if (!parse_positive_int_cap(qty_str, &stock_quantity)) {
    printf("Status: 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nInvalid stock_quantity\n");
    return 0;
  }

  sqlite3* db = NULL;
  sqlite3_stmt* stmt = NULL;

  if (sqlite3_open_v2("app.db", &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL) != SQLITE_OK) {
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDB open error\n");
    return 0;
  }

  char* err = NULL;
  if (sqlite3_exec(db, "BEGIN IMMEDIATE;", NULL, NULL, &err) != SQLITE_OK) {
    sqlite3_free(err);
    sqlite3_close(db);
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDB txn error\n");
    return 0;
  }

  const char* sql =
    "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?);";

  if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
    sqlite3_exec(db, "ROLLBACK;", NULL, NULL, NULL);
    sqlite3_close(db);
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDB prepare error\n");
    return 0;
  }

  sqlite3_bind_text(stmt, 1, username, -1, SQLITE_TRANSIENT);
  sqlite3_bind_text(stmt, 2, stock_name, -1, SQLITE_TRANSIENT);
  sqlite3_bind_int(stmt, 3, stock_quantity);

  int rc = sqlite3_step(stmt);
  sqlite3_finalize(stmt);

  if (rc != SQLITE_DONE) {
    sqlite3_exec(db, "ROLLBACK;", NULL, NULL, NULL);
    sqlite3_close(db);
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDB insert error\n");
    return 0;
  }

  if (sqlite3_exec(db, "COMMIT;", NULL, NULL, &err) != SQLITE_OK) {
    sqlite3_free(err);
    sqlite3_exec(db, "ROLLBACK;", NULL, NULL, NULL);
    sqlite3_close(db);
    printf("Status: 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nDB commit error\n");
    return 0;
  }

  sqlite3_close(db);

  // Fixed internal redirect only
  printf("Status: 303 See Other\r\nLocation: /profile\r\n\r\n");
  return 0;
}