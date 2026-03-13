/* C (libmicrohttpd + OpenSSL SHA-256)
   NOTE: In real systems, use POST (not URL/query params) + HTTPS to avoid credential leakage via logs, caches, referrers. */

#include <microhttpd.h>
#include <openssl/sha.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <arpa/inet.h>

#define PORT 8080

/* Demo credentials (hash stored, not plaintext) */
static const char *EXPECTED_USERNAME = "admin";
static const char *SALT = "static-demo-salt-change-me";

/* SHA-256("salt:password123") precomputed at startup for demo */
static unsigned char EXPECTED_PW_HASH[SHA256_DIGEST_LENGTH];

static void sha256_salt_password(const char *salt, const char *password, unsigned char out[SHA256_DIGEST_LENGTH]) {
  SHA256_CTX ctx;
  SHA256_Init(&ctx);
  SHA256_Update(&ctx, salt, strlen(salt));
  SHA256_Update(&ctx, ":", 1);
  SHA256_Update(&ctx, password, strlen(password));
  SHA256_Final(out, &ctx);
}

/* Constant-time memcmp */
static int ct_memeq(const unsigned char *a, const unsigned char *b, size_t n) {
  unsigned char diff = 0;
  for (size_t i = 0; i < n; i++) diff |= (unsigned char)(a[i] ^ b[i]);
  return diff == 0;
}

static int ct_streq(const char *a, const char *b) {
  size_t la = strlen(a), lb = strlen(b);
  size_t n = (la > lb) ? la : lb;
  if (n == 0) n = 1;
  unsigned char diff = 0;
  for (size_t i = 0; i < n; i++) {
    unsigned char ca = (i < la) ? (unsigned char)a[i] : 0;
    unsigned char cb = (i < lb) ? (unsigned char)b[i] : 0;
    diff |= (unsigned char)(ca ^ cb);
  }
  return (diff == 0) && (la == lb);
}

/* Simple in-memory per-IP rate limit: 5 requests per 60 seconds
   Fixed-size table for demo purposes. */
#define RL_SLOTS 2048
#define RL_MAX 5
#define RL_WINDOW 60

typedef struct {
  uint32_t ip;
  time_t window_start;
  int count;
  int used;
} rl_entry;

static rl_entry rl_table[RL_SLOTS];

static unsigned rl_hash(uint32_t ip) {
  ip ^= ip >> 16;
  ip *= 0x7feb352d;
  ip ^= ip >> 15;
  ip *= 0x846ca68b;
  ip ^= ip >> 16;
  return (unsigned)(ip % RL_SLOTS);
}

static int rate_limited(uint32_t ip) {
  time_t now = time(NULL);
  unsigned idx = rl_hash(ip);
  for (unsigned probe = 0; probe < RL_SLOTS; probe++) {
    rl_entry *e = &rl_table[(idx + probe) % RL_SLOTS];
    if (!e->used) {
      e->used = 1;
      e->ip = ip;
      e->window_start = now;
      e->count = 1;
      return 0;
    }
    if (e->ip == ip) {
      if (now - e->window_start >= RL_WINDOW) {
        e->window_start = now;
        e->count = 1;
        return 0;
      }
      if (e->count >= RL_MAX) return 1;
      e->count++;
      return 0;
    }
  }
  return 1; /* table full: fail closed */
}

static uint32_t get_client_ip_v4(struct MHD_Connection *connection) {
  const union MHD_ConnectionInfo *ci = MHD_get_connection_info(connection, MHD_CONNECTION_INFO_CLIENT_ADDRESS);
  if (!ci || !ci->client_addr) return 0;
  const struct sockaddr *sa = (const struct sockaddr *)ci->client_addr;
  if (sa->sa_family == AF_INET) {
    const struct sockaddr_in *sin = (const struct sockaddr_in *)sa;
    return sin->sin_addr.s_addr;
  }
  return 0;
}

static int send_text(struct MHD_Connection *connection, unsigned int status, const char *body) {
  struct MHD_Response *response = MHD_create_response_from_buffer(strlen(body),
                                                                  (void *)body,
                                                                  MHD_RESPMEM_MUST_COPY);
  if (!response) return MHD_NO;
  MHD_add_response_header(response, "Content-Type", "text/plain");
  int ret = MHD_queue_response(connection, status, response);
  MHD_destroy_response(response);
  return ret;
}

static int handler(void *cls,
                   struct MHD_Connection *connection,
                   const char *url,
                   const char *method,
                   const char *version,
                   const char *upload_data,
                   size_t *upload_data_size,
                   void **con_cls) {
  (void)cls; (void)version; (void)upload_data; (void)upload_data_size; (void)con_cls;

  if (strcmp(method, "GET") != 0) return send_text(connection, 405, "false");
  if (strcmp(url, "/check_mod") != 0) return send_text(connection, 404, "false");

  uint32_t ip = get_client_ip_v4(connection);
  if (rate_limited(ip)) return send_text(connection, 429, "false");

  const char *username = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "username");
  const char *password = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "password");
  if (!username) username = "";
  if (!password) password = "";

  /* Constant-time comparisons (avoid leaking which field was wrong) */
  int user_ok = ct_streq(username, EXPECTED_USERNAME);

  unsigned char pw_hash[SHA256_DIGEST_LENGTH];
  sha256_salt_password(SALT, password, pw_hash);
  int pw_ok = ct_memeq(pw_hash, EXPECTED_PW_HASH, SHA256_DIGEST_LENGTH);

  int ok = user_ok && pw_ok;
  return send_text(connection, ok ? 200 : 401, ok ? "true" : "false");
}

int main(void) {
  sha256_salt_password(SALT, "password123", EXPECTED_PW_HASH);

  struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD,
                                              PORT,
                                              NULL, NULL,
                                              &handler, NULL,
                                              MHD_OPTION_END);
  if (!daemon) {
    fprintf(stderr, "Failed to start server\n");
    return 1;
  }

  printf("Listening on http://127.0.0.1:%d/check_mod?username=...&password=...\n", PORT);
  getchar();

  MHD_stop_daemon(daemon);
  return 0;
}