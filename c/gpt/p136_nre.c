/* c (libmicrohttpd + OpenSSL)
   NOTE: Demo uses URL params as requested. In real systems, use POST + HTTPS; never send credentials in URLs (they get logged). */
#include <microhttpd.h>
#include <openssl/evp.h>
#include <openssl/crypto.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define PORT 8080

/* ---- Demo credentials (store hashes in real systems) ---- */
static const char *DEMO_USER = "admin";
static const uint8_t SALT[16] = {0xC0,0xFF,0xEE,0xC0,0xFF,0xEE,0xC0,0xFF,0xEE,0xC0,0xFF,0xEE,0xC0,0xFF,0xEE,0xC0};
static const int ITERATIONS = 200000;
static const int KEYLEN = 32; /* bytes */
static uint8_t DEMO_PW_HASH[KEYLEN];

/* ---- Basic in-memory rate limiting (per-IP sliding window) ---- */
#define MAX_IPS 1024
#define WINDOW_SEC 60
#define MAX_ATTEMPTS 20

typedef struct {
  uint32_t ip; /* IPv4 only for simplicity */
  time_t times[MAX_ATTEMPTS];
  int count;
} ip_bucket;

static ip_bucket buckets[MAX_IPS];

static uint32_t ip_to_u32(const struct sockaddr *sa) {
  if (!sa || sa->sa_family != AF_INET) return 0;
  const struct sockaddr_in *in = (const struct sockaddr_in *)sa;
  return ntohl(in->sin_addr.s_addr);
}

static int rate_limited(uint32_t ip) {
  time_t now = time(NULL);
  int slot = -1;
  for (int i = 0; i < MAX_IPS; i++) {
    if (buckets[i].ip == ip) { slot = i; break; }
    if (slot < 0 && buckets[i].ip == 0) slot = i;
  }
  if (slot < 0) slot = 0;

  if (buckets[slot].ip == 0) {
    buckets[slot].ip = ip;
    buckets[slot].count = 0;
  }

  /* prune */
  int new_count = 0;
  for (int i = 0; i < buckets[slot].count; i++) {
    if (difftime(now, buckets[slot].times[i]) <= WINDOW_SEC) {
      buckets[slot].times[new_count++] = buckets[slot].times[i];
    }
  }
  buckets[slot].count = new_count;

  if (buckets[slot].count >= MAX_ATTEMPTS) return 1;

  buckets[slot].times[buckets[slot].count++] = now;
  return 0;
}

static int ct_str_eq(const char *a, const char *b) {
  if (!a) a = "";
  if (!b) b = "";
  size_t la = strlen(a), lb = strlen(b);
  size_t max = (la > lb) ? la : lb;
  unsigned char *pa = (unsigned char *)calloc(max ? max : 1, 1);
  unsigned char *pb = (unsigned char *)calloc(max ? max : 1, 1);
  if (!pa || !pb) { free(pa); free(pb); return 0; }
  memcpy(pa, a, la);
  memcpy(pb, b, lb);
  int eq = (la == lb) && (CRYPTO_memcmp(pa, pb, max) == 0);
  /* Always run CRYPTO_memcmp on padded buffers */
  (void)CRYPTO_memcmp(pa, pb, max);
  free(pa); free(pb);
  return eq;
}

static void derive_pw_hash(const char *password, uint8_t out[KEYLEN]) {
  if (!password) password = "";
  PKCS5_PBKDF2_HMAC(password, (int)strlen(password), SALT, (int)sizeof(SALT),
                    ITERATIONS, EVP_sha256(), KEYLEN, out);
}

static int auth_ok(const char *user, const char *pw) {
  uint8_t pw_hash[KEYLEN];
  derive_pw_hash(pw, pw_hash);

  int user_ok = ct_str_eq(user, DEMO_USER);
  int pw_ok = (CRYPTO_memcmp(pw_hash, DEMO_PW_HASH, KEYLEN) == 0);

  /* Always compute both; generic response only. */
  (void)CRYPTO_memcmp(pw_hash, DEMO_PW_HASH, KEYLEN);
  return user_ok && pw_ok;
}

static int send_text(struct MHD_Connection *connection, const char *text) {
  struct MHD_Response *response = MHD_create_response_from_buffer(strlen(text),
                                  (void *)text, MHD_RESPMEM_PERSISTENT);
  if (!response) return MHD_NO;
  MHD_add_response_header(response, "Content-Type", "text/plain");
  int ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
  MHD_destroy_response(response);
  return ret;
}

static int handler(void *cls, struct MHD_Connection *connection,
                   const char *url, const char *method,
                   const char *version, const char *upload_data,
                   size_t *upload_data_size, void **con_cls) {
  (void)cls; (void)version; (void)upload_data; (void)upload_data_size; (void)con_cls;

  if (0 != strcmp(method, "GET")) return send_text(connection, "false");
  if (0 != strcmp(url, "/check_mod")) return send_text(connection, "false");

  const union MHD_ConnectionInfo *ci = MHD_get_connection_info(connection, MHD_CONNECTION_INFO_CLIENT_ADDRESS);
  uint32_t ip = ip_to_u32(ci ? ci->client_addr : NULL);
  if (rate_limited(ip)) return send_text(connection, "false");

  const char *username = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "username");
  const char *password = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "password");

  return send_text(connection, auth_ok(username, password) ? "true" : "false");
}

int main(void) {
  /* Precompute demo password hash */
  derive_pw_hash("correcthorsebatterystaple", DEMO_PW_HASH);

  struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, PORT, NULL, NULL,
                                              &handler, NULL, MHD_OPTION_END);
  if (!daemon) {
    fprintf(stderr, "Failed to start server\n");
    return 1;
  }
  printf("Listening on http://127.0.0.1:%d\n", PORT);
  printf("Press Enter to quit...\n");
  (void)getchar();
  MHD_stop_daemon(daemon);
  return 0;
}