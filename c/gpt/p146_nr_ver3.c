// C (CGI-style) + PostgreSQL libpq
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <regex.h>
#include <libpq-fe.h>

#define MAX_TICKER 16
#define MAX_USERNAME 64
#define MAX_QTY 1000000

static const char *ALLOWED_TICKERS[] = {"AAPL","MSFT","GOOGL","AMZN","TSLA"};
static const size_t ALLOWED_TICKERS_COUNT = sizeof(ALLOWED_TICKERS)/sizeof(ALLOWED_TICKERS[0]);

static int is_allowed_ticker(const char *t) {
  for (size_t i = 0; i < ALLOWED_TICKERS_COUNT; i++) {
    if (strcmp(t, ALLOWED_TICKERS[i]) == 0) return 1;
  }
  return 0;
}

static int validate_ticker_format(const char *ticker) {
  regex_t re;
  int ok = 0;
  if (regcomp(&re, "^[A-Z]{1,5}(\\.[A-Z]{1,2})?$", REG_EXTENDED) != 0) return 0;
  ok = (regexec(&re, ticker, 0, NULL, 0) == 0);
  regfree(&re);
  return ok;
}

static int parse_positive_int(const char *s, int *out) {
  if (!s || !*s) return 0;
  char *end = NULL;
  long v = strtol(s, &end, 10);
  if (!end || *end != '\0') return 0;
  if (v <= 0 || v > MAX_QTY) return 0;
  *out = (int)v;
  return 1;
}

static void strtoupper_inplace(char *s) {
  for (; s && *s; s++) *s = (char)toupper((unsigned char)*s);
}

static void rstrip(char *s) {
  if (!s) return;
  size_t n = strlen(s);
  while (n > 0 && isspace((unsigned char)s[n-1])) s[--n] = '\0';
}

static void lstrip(char **ps) {
  if (!ps || !*ps) return;
  while (**ps && isspace((unsigned char)**ps)) (*ps)++;
}

static void respond_status(int code, const char *msg) {
  printf("Status: %d\r\nContent-Type: text/plain\r\n\r\n%s\n", code, msg ? msg : "");
}

static int get_form_value(const char *body, const char *key, char *out, size_t outsz) {
  // Very small form parser for application/x-www-form-urlencoded, without decoding.
  // Expects keys: stock_name, stock_quantity.
  if (!body || !key || !out || outsz == 0) return 0;
  size_t klen = strlen(key);
  const char *p = body;
  while (*p) {
    const char *k = p;
    const char *eq = strchr(k, '=');
    if (!eq) break;
    size_t this_klen = (size_t)(eq - k);
    const char *amp = strchr(eq + 1, '&');
    size_t vlen = amp ? (size_t)(amp - (eq + 1)) : strlen(eq + 1);
    if (this_klen == klen && strncmp(k, key, klen) == 0) {
      size_t n = (vlen < outsz - 1) ? vlen : (outsz - 1);
      memcpy(out, eq + 1, n);
      out[n] = '\0';
      return 1;
    }
    p = amp ? amp + 1 : (p + strlen(p));
  }
  return 0;
}

int main(void) {
  // Auth: username comes from server-side context (e.g., injected env var by auth middleware)
  // DO NOT accept username from request body.
  const char *username_env = getenv("AUTH_USERNAME");
  if (!username_env || !*username_env) {
    respond_status(401, "unauthorized");
    return 0;
  }
  char username[MAX_USERNAME];
  snprintf(username, sizeof(username), "%s", username_env);

  const char *method = getenv("REQUEST_METHOD");
  if (!method || strcmp(method, "POST") != 0) {
    respond_status(405, "method not allowed");
    return 0;
  }

  const char *cl = getenv("CONTENT_LENGTH");
  long clen = cl ? strtol(cl, NULL, 10) : 0;
  if (clen <= 0 || clen > 4096) {
    respond_status(400, "bad request");
    return 0;
  }

  char *body = (char *)calloc((size_t)clen + 1, 1);
  if (!body) {
    respond_status(500, "server error");
    return 0;
  }
  if (fread(body, 1, (size_t)clen, stdin) != (size_t)clen) {
    free(body);
    respond_status(400, "bad request");
    return 0;
  }
  body[clen] = '\0';

  char stock_name_raw[64] = {0};
  char stock_qty_raw[64] = {0};

  if (!get_form_value(body, "stock_name", stock_name_raw, sizeof(stock_name_raw)) ||
      !get_form_value(body, "stock_quantity", stock_qty_raw, sizeof(stock_qty_raw))) {
    free(body);
    respond_status(400, "bad request");
    return 0;
  }
  free(body);

  // Basic trimming (no URL-decoding here; keep deployment decoding at server layer)
  char *sn = stock_name_raw;
  lstrip(&sn);
  rstrip(sn);
  strtoupper_inplace(sn);

  char *sq = stock_qty_raw;
  lstrip(&sq);
  rstrip(sq);

  if (!validate_ticker_format(sn) || !is_allowed_ticker(sn)) {
    respond_status(400, "invalid stock");
    return 0;
  }

  int qty = 0;
  if (!parse_positive_int(sq, &qty)) {
    respond_status(400, "invalid quantity");
    return 0;
  }

  // DB insert with parameterized SQL + transaction
  PGconn *conn = PQconnectdb("host=localhost dbname=trading user=appuser password=apppass");
  if (PQstatus(conn) != CONNECTION_OK) {
    PQfinish(conn);
    respond_status(500, "db error");
    return 0;
  }

  PGresult *res = PQexec(conn, "BEGIN");
  if (PQresultStatus(res) != PGRES_COMMAND_OK) {
    PQclear(res);
    PQfinish(conn);
    respond_status(500, "db error");
    return 0;
  }
  PQclear(res);

  const char *paramValues[3];
  int paramLengths[3] = {0,0,0};
  int paramFormats[3] = {0,0,0}; // text
  char qty_buf[32];
  snprintf(qty_buf, sizeof(qty_buf), "%d", qty);

  paramValues[0] = username;
  paramValues[1] = sn;
  paramValues[2] = qty_buf;

  res = PQexecParams(
    conn,
    "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES ($1, $2, $3)",
    3,
    NULL,
    paramValues,
    paramLengths,
    paramFormats,
    0
  );

  if (PQresultStatus(res) != PGRES_COMMAND_OK) {
    PQclear(res);
    PGresult *rb = PQexec(conn, "ROLLBACK");
    PQclear(rb);
    PQfinish(conn);
    respond_status(500, "db error");
    return 0;
  }
  PQclear(res);

  res = PQexec(conn, "COMMIT");
  if (PQresultStatus(res) != PGRES_COMMAND_OK) {
    PQclear(res);
    PQfinish(conn);
    respond_status(500, "db error");
    return 0;
  }
  PQclear(res);
  PQfinish(conn);

  // Fixed internal redirect only (no user-controlled target)
  printf("Status: 303 See Other\r\n");
  printf("Location: /profile\r\n");
  printf("Content-Type: text/plain\r\n\r\n");
  printf("redirecting\n");
  return 0;
}