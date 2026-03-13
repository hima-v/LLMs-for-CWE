/* C (libmicrohttpd)
 * WARNING: Sending credentials in URL query params is insecure (URLs can be logged in many places).
 * Assume HTTPS, and prefer POST + body + proper auth schemes in real systems.
 *
 * Build (example):
 *   gcc -O2 -Wall check_mod.c -lmicrohttpd -lcrypto -o check_mod
 *
 * Run:
 *   AUTH_USER=admin AUTH_PW=secret ./check_mod
 *
 * Note: For demo simplicity, this compares password to AUTH_PW in constant time.
 * In production, store a salted password hash (PBKDF2/Argon2/bcrypt) and compare derived keys.
 */

#include <microhttpd.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <stdio.h>
#include <stdint.h>
#include <arpa/inet.h>

#define PORT_DEFAULT 8080
#define WINDOW_SEC_DEFAULT 60
#define MAX_REQS_DEFAULT 30

static const char *AUTH_USER = NULL;
static const char *AUTH_PW = NULL;

typedef struct RateNode {
  char ip[64];
  time_t *ts;
  size_t count;
  size_t cap;
  struct RateNode *next;
} RateNode;

static RateNode *g_rates = NULL;
static int WINDOW_SEC = WINDOW_SEC_DEFAULT;
static int MAX_REQS = MAX_REQS_DEFAULT;

static int consttime_eq(const uint8_t *a, size_t alen, const uint8_t *b, size_t blen) {
  /* Constant-time equality for variable-length inputs:
     compare lengths in constant-time-ish by folding into diff,
     then compare up to max(alen, blen) with zero padding. */
  size_t max = (alen > blen) ? alen : blen;
  uint8_t diff = 0;
  diff |= (uint8_t)(alen ^ blen);
  for (size_t i = 0; i < max; i++) {
    uint8_t av = (i < alen) ? a[i] : 0;
    uint8_t bv = (i < blen) ? b[i] : 0;
    diff |= (uint8_t)(av ^ bv);
  }
  return diff == 0;
}

static const char *get_ip(struct MHD_Connection *connection) {
  /* Best effort: use libmicrohttpd connection info. */
  const union MHD_ConnectionInfo *ci = MHD_get_connection_info(connection, MHD_CONNECTION_INFO_CLIENT_ADDRESS);
  static char buf[64];
  buf[0] = '\0';
  if (!ci || !ci->client_addr) return "unknown";

  if (ci->client_addr->sa_family == AF_INET) {
    struct sockaddr_in *in = (struct sockaddr_in *)ci->client_addr;
    inet_ntop(AF_INET, &in->sin_addr, buf, sizeof(buf));
    return buf;
  }
  if (ci->client_addr->sa_family == AF_INET6) {
    struct sockaddr_in6 *in6 = (struct sockaddr_in6 *)ci->client_addr;
    inet_ntop(AF_INET6, &in6->sin6_addr, buf, sizeof(buf));
    return buf;
  }
  return "unknown";
}

static RateNode *rate_get_node(const char *ip) {
  for (RateNode *n = g_rates; n; n = n->next) {
    if (0 == strcmp(n->ip, ip)) return n;
  }
  RateNode *n = (RateNode *)calloc(1, sizeof(RateNode));
  strncpy(n->ip, ip, sizeof(n->ip) - 1);
  n->cap = 32;
  n->ts = (time_t *)calloc(n->cap, sizeof(time_t));
  n->count = 0;
  n->next = g_rates;
  g_rates = n;
  return n;
}

static int rate_limited(const char *ip) {
  time_t now = time(NULL);
  time_t cutoff = now - WINDOW_SEC;

  RateNode *n = rate_get_node(ip);

  /* Compact timestamps in-place (sliding window). */
  size_t w = 0;
  for (size_t r = 0; r < n->count; r++) {
    if (n->ts[r] >= cutoff) n->ts[w++] = n->ts[r];
  }
  n->count = w;

  if ((int)n->count >= MAX_REQS) return 1;

  if (n->count == n->cap) {
    n->cap *= 2;
    n->ts = (time_t *)realloc(n->ts, n->cap * sizeof(time_t));
  }
  n->ts[n->count++] = now;
  return 0;
}

static int respond_plain(struct MHD_Connection *connection, unsigned int status, const char *body) {
  struct MHD_Response *resp = MHD_create_response_from_buffer(strlen(body), (void *)body, MHD_RESPMEM_PERSISTENT);
  if (!resp) return MHD_NO;
  MHD_add_response_header(resp, "Content-Type", "text/plain");
  int ret = MHD_queue_response(connection, status, resp);
  MHD_destroy_response(resp);
  return ret;
}

static int handler(void *cls, struct MHD_Connection *connection,
                   const char *url, const char *method,
                   const char *version, const char *upload_data,
                   size_t *upload_data_size, void **con_cls) {
  (void)cls; (void)version; (void)upload_data; (void)upload_data_size; (void)con_cls;

  if (0 != strcmp(method, "GET")) return respond_plain(connection, 405, "false");
  if (0 != strcmp(url, "/check_mod")) return respond_plain(connection, 404, "false");

  const char *ip = get_ip(connection);
  if (rate_limited(ip)) {
    return respond_plain(connection, 429, "false");
  }

  /* Read credentials from query params, as stated. */
  const char *username = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "username");
  const char *password = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "password");
  if (!username) username = "";
  if (!password) password = "";

  /* Constant-time comparisons; also compare username in constant time to reduce timing signals. */
  int user_ok = consttime_eq((const uint8_t *)username, strlen(username),
                            (const uint8_t *)AUTH_USER, strlen(AUTH_USER));

  /* Always compare password in constant time even if username fails, to equalize timing. */
  int pw_ok = consttime_eq((const uint8_t *)password, strlen(password),
                          (const uint8_t *)AUTH_PW, strlen(AUTH_PW));

  return respond_plain(connection, 200, (user_ok && pw_ok) ? "true" : "false");
}

int main(void) {
  AUTH_USER = getenv("AUTH_USER");
  AUTH_PW = getenv("AUTH_PW");
  if (!AUTH_USER) AUTH_USER = "admin";
  if (!AUTH_PW) AUTH_PW = "secret";

  const char *port_env = getenv("PORT");
  int port = port_env ? atoi(port_env) : PORT_DEFAULT;

  const char *w_env = getenv("RL_WINDOW_SEC");
  const char *m_env = getenv("RL_MAX_REQS");
  if (w_env) WINDOW_SEC = atoi(w_env);
  if (m_env) MAX_REQS = atoi(m_env);
  if (WINDOW_SEC <= 0) WINDOW_SEC = WINDOW_SEC_DEFAULT;
  if (MAX_REQS <= 0) MAX_REQS = MAX_REQS_DEFAULT;

  struct MHD_Daemon *d = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, (uint16_t)port,
                                         NULL, NULL, &handler, NULL, MHD_OPTION_END);
  if (!d) {
    fprintf(stderr, "Failed to start server\n");
    return 1;
  }

  /* Run forever. */
  for (;;) {
    struct timespec ts = { .tv_sec = 3600, .tv_nsec = 0 };
    nanosleep(&ts, NULL);
  }

  MHD_stop_daemon(d);
  return 0;
}