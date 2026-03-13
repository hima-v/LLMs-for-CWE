#include <ctype.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>

typedef struct {
  const char *query;   // e.g., "name=Alice"
  size_t query_len;
} Request;

typedef struct {
  int status;
  const char *content_type;
  char body[1024];
} Response;

typedef struct {
  const char *home_template; // allowlisted/fixed
} Environment;

static int parse_query_param(const char *query, size_t qlen, const char *key,
                            char *out, size_t out_cap) {
  size_t key_len = strlen(key);
  size_t i = 0;
  while (i < qlen) {
    size_t start = i;
    while (i < qlen && query[i] != '&') i++;
    size_t end = i;
    if (end > start) {
      if (end - start > key_len + 1 && strncmp(query + start, key, key_len) == 0 &&
          query[start + key_len] == '=') {
        size_t val_len = end - (start + key_len + 1);
        if (val_len >= out_cap) val_len = out_cap - 1;
        memcpy(out, query + start + key_len + 1, val_len);
        out[val_len] = '\0';
        return 1;
      }
    }
    if (i < qlen && query[i] == '&') i++;
  }
  return 0;
}

static int is_allowed_name_char(unsigned char c) {
  return (isalnum(c) || c == ' ' || c == '_' || c == '.' || c == '-');
}

static void safe_name(const char *raw, char *out, size_t out_cap) {
  const char *fallback = "Guest";
  if (!raw) {
    snprintf(out, out_cap, "%s", fallback);
    return;
  }
  while (*raw && isspace((unsigned char)*raw)) raw++;

  char tmp[64];
  size_t n = 0;
  while (raw[n] && n < sizeof(tmp) - 1) n++;
  while (n > 0 && isspace((unsigned char)raw[n - 1])) n--;

  if (n == 0 || n > 32) {
    snprintf(out, out_cap, "%s", fallback);
    return;
  }
  if (!isalpha((unsigned char)raw[0])) {
    snprintf(out, out_cap, "%s", fallback);
    return;
  }
  for (size_t i = 0; i < n; i++) {
    unsigned char c = (unsigned char)raw[i];
    if (!is_allowed_name_char(c)) {
      snprintf(out, out_cap, "%s", fallback);
      return;
    }
    tmp[i] = (char)c;
  }
  tmp[n] = '\0';
  snprintf(out, out_cap, "%s", tmp);
}

static size_t escape_html(const char *in, char *out, size_t out_cap) {
  size_t w = 0;
  for (size_t i = 0; in[i] != '\0'; i++) {
    const char *rep = NULL;
    switch (in[i]) {
      case '&': rep = "&amp;"; break;
      case '<': rep = "&lt;"; break;
      case '>': rep = "&gt;"; break;
      case '"': rep = "&quot;"; break;
      case '\'': rep = "&#x27;"; break;
      default: rep = NULL;
    }
    if (rep) {
      size_t rlen = strlen(rep);
      if (w + rlen >= out_cap) break;
      memcpy(out + w, rep, rlen);
      w += rlen;
    } else {
      if (w + 1 >= out_cap) break;
      out[w++] = in[i];
    }
  }
  if (out_cap) out[w < out_cap ? w : out_cap - 1] = '\0';
  return w;
}

Response render_response_from_env(const Environment *env, const Request *req) {
  Response resp;
  resp.status = 200;
  resp.content_type = "text/html; charset=utf-8";

  char raw_name[128] = {0};
  (void)parse_query_param(req->query, req->query_len, "name", raw_name, sizeof(raw_name));

  char name[64];
  safe_name(raw_name[0] ? raw_name : NULL, name, sizeof(name));

  char esc[256];
  escape_html(name, esc, sizeof(esc));

  // fixed template (no user-controlled selection), user value is data-only and HTML-escaped
  snprintf(resp.body, sizeof(resp.body), env->home_template, esc);
  return resp;
}

Response home(const Environment *env, const Request *req) {
  return render_response_from_env(env, req);
}

/* Example template:
   const char *tpl = "<!doctype html><html><body><h1>Hello, %s</h1></body></html>";
*/