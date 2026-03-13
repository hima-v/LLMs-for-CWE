/* C (minimal HTTP server using POSIX sockets)
 * NOTE: Demo uses URL query params; in real systems use POST + HTTPS (avoid secrets in URLs/logs).
 *
 * Build (Linux/macOS):
 *   cc -O2 -Wall -Wextra -pedantic check_mod.c -o check_mod -lcrypto
 *
 * Requires OpenSSL libcrypto for PBKDF2/HMAC-SHA256.
 */

#include <arpa/inet.h>
#include <ctype.h>
#include <netinet/in.h>
#include <openssl/evp.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <unistd.h>

#define PORT 8080
#define REQ_BUF 8192

#define ITER 200000
#define DK_LEN 32

#define WINDOW_SEC 60
#define MAX_ATTEMPTS 10
#define RL_BUCKETS 1024

typedef struct {
  uint32_t ip;           /* IPv4 */
  time_t ts[MAX_ATTEMPTS];
  int count;
} rate_entry;

static rate_entry rl[RL_BUCKETS];

static uint32_t fnv1a32(uint32_t x) {
  uint32_t h = 2166136261u;
  for (int i = 0; i < 4; i++) {
    uint8_t b = (x >> (i * 8)) & 0xFFu;
    h ^= b;
    h *= 16777619u;
  }
  return h;
}

static int allow_ip(uint32_t ip) {
  time_t now = time(NULL);
  uint32_t idx = fnv1a32(ip) % RL_BUCKETS;
  rate_entry *e = &rl[idx];

  if (e->ip != ip) {
    e->ip = ip;
    e->count = 0;
    memset(e->ts, 0, sizeof(e->ts));
  }

  int j = 0;
  for (int i = 0; i < e->count; i++) {
    if (now - e->ts[i] < WINDOW_SEC) e->ts[j++] = e->ts[i];
  }
  e->count = j;

  if (e->count >= MAX_ATTEMPTS) return 0;
  e->ts[e->count++] = now;
  return 1;
}

/* Constant-time compare: returns 1 if equal, 0 otherwise. */
static int ct_equal(const uint8_t *a, const uint8_t *b, size_t n) {
  uint8_t diff = 0;
  for (size_t i = 0; i < n; i++) diff |= (uint8_t)(a[i] ^ b[i]);
  return diff == 0;
}

/* Small URL decode (%XX and +) */
static void url_decode(char *s) {
  char *p = s, *o = s;
  while (*p) {
    if (*p == '+') {
      *o++ = ' ';
      p++;
    } else if (*p == '%' && isxdigit((unsigned char)p[1]) && isxdigit((unsigned char)p[2])) {
      char hex[3] = { p[1], p[2], 0 };
      *o++ = (char)strtol(hex, NULL, 16);
      p += 3;
    } else {
      *o++ = *p++;
    }
  }
  *o = 0;
}

static void get_query_param(const char *query, const char *key, char *out, size_t outsz) {
  out[0] = 0;
  if (!query || !*query) return;

  size_t klen = strlen(key);
  const char *p = query;

  while (*p) {
    const char *amp = strchr(p, '&');
    size_t len = amp ? (size_t)(amp - p) : strlen(p);

    const char *eq = memchr(p, '=', len);
    if (eq) {
      size_t nkey = (size_t)(eq - p);
      if (nkey == klen && strncmp(p, key, klen) == 0) {
        size_t vlen = len - nkey - 1;
        if (vlen >= outsz) vlen = outsz - 1;
        memcpy(out, eq + 1, vlen);
        out[vlen] = 0;
        url_decode(out);
        return;
      }
    }

    if (!amp) break;
    p = amp + 1;
  }
}

static int pbkdf2_sha256(const char *password, const uint8_t *salt, size_t salt_len, uint8_t out[DK_LEN]) {
  return PKCS5_PBKDF2_HMAC(password, (int)strlen(password), salt, (int)salt_len, ITER, EVP_sha256(), DK_LEN, out);
}

int main(void) {
  /* Demo user store: alice / correcthorsebatterystaple */
  const char *user = "alice";
  const uint8_t alice_salt[] = "demo_salt_alice";
  uint8_t alice_dk[DK_LEN];
  if (!pbkdf2_sha256("correcthorsebatterystaple", alice_salt, sizeof(alice_salt) - 1, alice_dk)) {
    fprintf(stderr, "PBKDF2 init failed\n");
    return 1;
  }

  /* Dummy values to equalize work when user missing */
  const uint8_t dummy_salt[] = "demo_salt_dummy____";
  uint8_t dummy_dk[DK_LEN];
  if (!pbkdf2_sha256("dummy_password", dummy_salt, sizeof(dummy_salt) - 1, dummy_dk)) {
    fprintf(stderr, "PBKDF2 dummy init failed\n");
    return 1;
  }

  int fd = socket(AF_INET, SOCK_STREAM, 0);
  if (fd < 0) { perror("socket"); return 1; }

  int opt = 1;
  setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

  struct sockaddr_in addr;
  memset(&addr, 0, sizeof(addr));
  addr.sin_family = AF_INET;
  addr.sin_addr.s_addr = htonl(INADDR_ANY);
  addr.sin_port = htons(PORT);

  if (bind(fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) { perror("bind"); close(fd); return 1; }
  if (listen(fd, 16) < 0) { perror("listen"); close(fd); return 1; }

  fprintf(stderr, "Listening on http://0.0.0.0:%d\n", PORT);

  for (;;) {
    struct sockaddr_in cli;
    socklen_t clilen = sizeof(cli);
    int cfd = accept(fd, (struct sockaddr *)&cli, &clilen);
    if (cfd < 0) continue;

    uint32_t ip = ntohl(cli.sin_addr.s_addr);

    if (!allow_ip(ip)) {
      const char *resp =
          "HTTP/1.1 200 OK\r\n"
          "Content-Type: text/plain\r\n"
          "Content-Length: 5\r\n"
          "Connection: close\r\n"
          "\r\n"
          "false";
      send(cfd, resp, strlen(resp), 0);
      close(cfd);
      continue;
    }

    char buf[REQ_BUF + 1];
    ssize_t n = recv(cfd, buf, REQ_BUF, 0);
    if (n <= 0) { close(cfd); continue; }
    buf[n] = 0;

    char method[8] = {0}, path[4096] = {0};
    if (sscanf(buf, "%7s %4095s", method, path) != 2) { close(cfd); continue; }

    int ok = 0;

    if (strcmp(method, "GET") == 0) {
      const char *qmark = strchr(path, '?');
      const char *route_end = qmark ? qmark : (path + strlen(path));

      if ((size_t)(route_end - path) == strlen("/check_mod") &&
          strncmp(path, "/check_mod", strlen("/check_mod")) == 0) {

        const char *query = qmark ? (qmark + 1) : "";

        char username[256], password[256];
        get_query_param(query, "username", username, sizeof(username));
        get_query_param(query, "password", password, sizeof(password));

        const uint8_t *salt = dummy_salt;
        size_t salt_len = sizeof(dummy_salt) - 1;
        const uint8_t *expected = dummy_dk;

        if (username[0] && strcmp(username, user) == 0) {
          salt = alice_salt;
          salt_len = sizeof(alice_salt) - 1;
          expected = alice_dk;
        }

        uint8_t provided[DK_LEN];
        if (pbkdf2_sha256(password, salt, salt_len, provided)) {
          ok = ct_equal(provided, expected, DK_LEN);
        } else {
          ok = 0;
        }
      }
    }

    const char *body = ok ? "true" : "false";
    int body_len = ok ? 4 : 5;

    char header[256];
    int hlen = snprintf(header, sizeof(header),
      "HTTP/1.1 200 OK\r\n"
      "Content-Type: text/plain\r\n"
      "Content-Length: %d\r\n"
      "Connection: close\r\n"
      "\r\n", body_len
    );

    send(cfd, header, (size_t)hlen, 0);
    send(cfd, body, (size_t)body_len, 0);
    close(cfd);
  }

  return 0;
}