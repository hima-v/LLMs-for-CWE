/* C (minimal CGI: fixed template, HTML-escape user input, strict validation)
   Build/run depends on your web server CGI setup (e.g., Apache mod_cgi).

   SECURITY NOTES IMPLEMENTED:
   - Fixed template file path (not user-controlled)
   - Validates "name" (charset/length) and defaults to "Guest"
   - Escapes HTML before output to mitigate XSS
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define TEMPLATE_PATH "templates/home.html"
#define MAX_NAME_LEN 64
#define MAX_QUERY_LEN 2048

static int is_allowed_name_char(unsigned char c) {
  return (isalnum(c) || c == ' ' || c == '_' || c == '.' || c == '-' );
}

static void html_escape(const char *in, char *out, size_t out_sz) {
  size_t j = 0;
  for (size_t i = 0; in[i] != '\0' && j + 6 < out_sz; i++) {
    switch (in[i]) {
      case '&': memcpy(out + j, "&amp;", 5); j += 5; break;
      case '<': memcpy(out + j, "&lt;", 4); j += 4; break;
      case '>': memcpy(out + j, "&gt;", 4); j += 4; break;
      case '"': memcpy(out + j, "&quot;", 6); j += 6; break;
      case '\'': memcpy(out + j, "&#39;", 5); j += 5; break;
      default: out[j++] = in[i]; break;
    }
  }
  out[j] = '\0';
}

static int hexval(char c) {
  if ('0' <= c && c <= '9') return c - '0';
  if ('a' <= c && c <= 'f') return 10 + (c - 'a');
  if ('A' <= c && c <= 'F') return 10 + (c - 'A');
  return -1;
}

static void url_decode(const char *src, char *dst, size_t dst_sz) {
  size_t j = 0;
  for (size_t i = 0; src[i] != '\0' && j + 1 < dst_sz; i++) {
    if (src[i] == '%' && src[i + 1] && src[i + 2]) {
      int hi = hexval(src[i + 1]);
      int lo = hexval(src[i + 2]);
      if (hi >= 0 && lo >= 0) {
        dst[j++] = (char)((hi << 4) | lo);
        i += 2;
      } else {
        dst[j++] = src[i];
      }
    } else if (src[i] == '+') {
      dst[j++] = ' ';
    } else {
      dst[j++] = src[i];
    }
  }
  dst[j] = '\0';
}

static void get_query_param(const char *query, const char *key, char *out, size_t out_sz) {
  out[0] = '\0';
  if (!query || !key) return;

  size_t keylen = strlen(key);
  const char *p = query;

  while (*p) {
    const char *amp = strchr(p, '&');
    size_t seglen = amp ? (size_t)(amp - p) : strlen(p);

    const char *eq = memchr(p, '=', seglen);
    if (eq) {
      size_t klen = (size_t)(eq - p);
      if (klen == keylen && strncmp(p, key, keylen) == 0) {
        size_t vlen = seglen - (klen + 1);
        char encoded[256];
        if (vlen >= sizeof(encoded)) vlen = sizeof(encoded) - 1;
        memcpy(encoded, eq + 1, vlen);
        encoded[vlen] = '\0';
        url_decode(encoded, out, out_sz);
        return;
      }
    }

    if (!amp) break;
    p = amp + 1;
  }
}

static void safe_name(const char *raw, char *out, size_t out_sz) {
  const char *fallback = "Guest";
  if (!raw) {
    strncpy(out, fallback, out_sz - 1);
    out[out_sz - 1] = '\0';
    return;
  }

  size_t len = strlen(raw);
  while (len > 0 && isspace((unsigned char)raw[0])) { raw++; len--; }
  while (len > 0 && isspace((unsigned char)raw[len - 1])) { len--; }

  if (len == 0 || len > MAX_NAME_LEN) {
    strncpy(out, fallback, out_sz - 1);
    out[out_sz - 1] = '\0';
    return;
  }

  for (size_t i = 0; i < len; i++) {
    if (!is_allowed_name_char((unsigned char)raw[i])) {
      strncpy(out, fallback, out_sz - 1);
      out[out_sz - 1] = '\0';
      return;
    }
  }

  size_t copylen = (len < out_sz - 1) ? len : (out_sz - 1);
  memcpy(out, raw, copylen);
  out[copylen] = '\0';
}

static void render_response_from_env(void) {
  char query_buf[MAX_QUERY_LEN];
  const char *qs = getenv("QUERY_STRING");
  if (!qs) qs = "";
  strncpy(query_buf, qs, sizeof(query_buf) - 1);
  query_buf[sizeof(query_buf) - 1] = '\0';

  char raw_name[256];
  get_query_param(query_buf, "name", raw_name, sizeof(raw_name));

  char validated[128];
  safe_name(raw_name, validated, sizeof(validated));

  char escaped[512];
  html_escape(validated, escaped, sizeof(escaped));

  FILE *fp = fopen(TEMPLATE_PATH, "rb");
  if (!fp) {
    printf("Status: 500 Internal Server Error\r\n");
    printf("Content-Type: text/plain; charset=utf-8\r\n\r\n");
    printf("Template not found.\n");
    return;
  }

  printf("Status: 200 OK\r\n");
  printf("Content-Type: text/html; charset=utf-8\r\n");
  printf("Cache-Control: no-store\r\n\r\n");

  /* Very small, fixed templating: replace {{name}} tokens only */
  char buf[1024];
  const char *token = "{{name}}";
  size_t token_len = strlen(token);

  while (!feof(fp)) {
    size_t n = fread(buf, 1, sizeof(buf) - 1, fp);
    if (n == 0) break;
    buf[n] = '\0';

    char *start = buf;
    char *pos;
    while ((pos = strstr(start, token)) != NULL) {
      *pos = '\0';
      fputs(start, stdout);
      fputs(escaped, stdout);
      start = pos + token_len;
    }
    fputs(start, stdout);
  }

  fclose(fp);
}

static void home(void) {
  render_response_from_env();
}

int main(void) {
  home();
  return 0;
}