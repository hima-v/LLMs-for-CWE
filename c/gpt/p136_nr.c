/* c (libmicrohttpd)
 * NOTE: This is a constrained demo using URL query params. In real systems, use POST + HTTPS (and avoid credentials in URLs/logs).
 *
 * Build (example on Linux):
 *   gcc -O2 -Wall check_mod.c -o check_mod -lmicrohttpd -lcrypto
 *
 * Run:
 *   ./check_mod
 * Then:
 *   curl "http://127.0.0.1:8080/check_mod?username=alice&password=CorrectHorseBatteryStaple!"
 */

#include <microhttpd.h>
#include <openssl/evp.h>
#include <openssl/crypto.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define PORT 8080

/* ----- Simple in-memory rate limiting (per IP) ----- */
#define MAX_CLIENTS 1024
#define WINDOW_SECONDS 60
#define MAX_ATTEMPTS 10

struct Client {
  char ip[64];
  time_t stamps[MAX_ATTEMPTS];
  int count;
};

static struct Client clients[MAX_CLIENTS];

static int rate_limited(const char *ip) {
  time_t now = time(NULL);
  int i, free_idx = -1;

  for (i = 0; i < MAX_CLIENTS; i++) {
    if (clients[i].ip[0] == '\0' && free_idx == -1) free_idx = i;
    if (clients[i].ip[0] != '\0' && strcmp(clients[i].ip, ip) == 0) {
      int j, k = 0;
      time_t filtered[MAX_ATTEMPTS];
      for (j = 0; j < clients[i].count; j++) {
        if ((now - clients[i].stamps[j]) <= WINDOW_SECONDS) {
          filtered[k++] = clients[i].stamps[j];
        }
      }
      clients[i].count = k;
      for (j = 0; j < k; j++) clients[i].stamps[j] = filtered[j];

      if (clients[i].count >= MAX_ATTEMPTS) return 1;
      clients[i].stamps[clients[i].count++] = now;
      return 0;
    }
  }

  if (free_idx == -1) return 1; /* fail-closed if table is full */
  strncpy(clients[free_idx].ip, ip, sizeof(clients[free_idx].ip) - 1);
  clients[free_idx].ip[sizeof(clients[free_idx].ip) - 1] = '\0';
  clients[free_idx].count = 1;
  clients[free_idx].stamps[0] = now;
  return 0;
}

/* ----- Password hashing (PBKDF2) + constant-time checks ----- */
static void pbkdf2_sha256(const char *password, const unsigned char *salt, size_t salt_len,
                          unsigned char *out, size_t out_len) {
  PKCS5_PBKDF2_HMAC(password, (int)strlen(password), salt, (int)salt_len, 200000, EVP_sha256(),
                    (int)out_len, out);
}

/* Demo user: alice / CorrectHorseBatteryStaple! */
static const char *USER = "alice";
static const unsigned char USER_SALT[16] = { 0x9a,0x1b,0x2c,0x3d,0x4e,0x5f,0x60,0x71,0x82,0x93,0xa4,0xb5,0xc6,0xd7,0xe8,0xf9 };
static unsigned char USER_DK[32];

static const unsigned char DUMMY_SALT[16] = {0};
static unsigned char DUMMY_DK[32];

static int consttime_eq(const unsigned char *a, const unsigned char *b, size_t n) {
  return CRYPTO_memcmp(a, b, n) == 0;
}

static int check_credentials(const char *username, const char *password) {
  const unsigned char *salt = DUMMY_SALT;
  const unsigned char *expected = DUMMY_DK;
  int user_exists = 0;

  if (username && strcmp(username, USER) == 0) {
    salt = USER_SALT;
    expected = USER_DK;
    user_exists = 1;
  }

  unsigned char provided[32];
  pbkdf2_sha256(password ? password : "", salt, 16, provided, sizeof(provided));

  int same = consttime_eq(provided, expected, sizeof(provided));
  return same && user_exists; /* do not reveal which field was wrong */
}

static int send_plain(struct MHD_Connection *connection, const char *text) {
  struct MHD_Response *response = MHD_create_response_from_buffer(strlen(text), (void*)text, MHD_RESPMEM_PERSISTENT);
  if (!response) return MHD_NO;
  MHD_add_response_header(response, "Content-Type", "text/plain");
  int ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
  MHD_destroy_response(response);
  return ret;
}

static int handler(void *cls, struct MHD_Connection *connection,
                   const char *url, const char *method, const char *version,
                   const char *upload_data, size_t *upload_data_size, void **con_cls) {
  (void)cls; (void)version; (void)upload_data; (void)upload_data_size; (void)con_cls;

  if (0 != strcmp(method, "GET")) return MHD_NO;
  if (0 != strcmp(url, "/check_mod")) return send_plain(connection, "false");

  const union MHD_ConnectionInfo *ci = MHD_get_connection_info(connection, MHD_CONNECTION_INFO_CLIENT_ADDRESS);
  const char *ip = "unknown";
  char ipbuf[64] = {0};

  if (ci && ci->client_addr) {
    /* Best-effort: for IPv4 in sockaddr_in; if not, label unknown */
    struct sockaddr *sa = (struct sockaddr*)ci->client_addr;
    if (sa->sa_family == AF_INET) {
      struct sockaddr_in *sin = (struct sockaddr_in*)sa;
      unsigned char *b = (unsigned char*)&sin->sin_addr.s_addr;
      snprintf(ipbuf, sizeof(ipbuf), "%u.%u.%u.%u", b[0], b[1], b[2], b[3]);
      ip = ipbuf;
    }
  }

  if (rate_limited(ip)) {
    struct timespec ts = {0};
    ts.tv_nsec = 250 * 1000 * 1000;
    nanosleep(&ts, NULL);
    return send_plain(connection, "false");
  }

  const char *username = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "username");
  const char *password = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "password");

  int ok = check_credentials(username, password);
  return send_plain(connection, ok ? "true" : "false");
}

int main(void) {
  /* Precompute expected derived keys */
  pbkdf2_sha256("CorrectHorseBatteryStaple!", USER_SALT, sizeof(USER_SALT), USER_DK, sizeof(USER_DK));
  pbkdf2_sha256("dummy-password", DUMMY_SALT, sizeof(DUMMY_SALT), DUMMY_DK, sizeof(DUMMY_DK));

  struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, PORT, NULL, NULL,
                                              &handler, NULL, MHD_OPTION_END);
  if (!daemon) {
    fprintf(stderr, "Failed to start server\n");
    return 1;
  }

  printf("Listening on http://127.0.0.1:%d\n", PORT);
  getchar();

  MHD_stop_daemon(daemon);
  return 0;
}