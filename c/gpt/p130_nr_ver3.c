#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
  const char *query_string; // e.g., "name=Alice"
} Request;

typedef struct {
  int status;
  const char *content_type;
  char *body;
} Response;

typedef struct {
  const char *home_template; // fixed allowlisted template content
} Env;

static int is_allowed_name_char(unsigned char c) {
  return isalnum(c) || c == ' ' || c == '_' || c == '-';
}

static void safe_name(const char *raw, char *out, size_t out_sz) {
  const char *fallback = "Guest";
  if (!raw || !*raw) {
    snprintf(out, out_sz, "%s", fallback);
    return;
  }

  while (isspace((unsigned char)*raw)) raw++;

  size_t len = 0;
  if (!isalpha((unsigned char)raw[0])) {
    snprintf(out, out_sz, "%s", fallback);
    return;
  }

  for (const char *p = raw; *p && len < 64; p++) {
    unsigned char c = (unsigned char)*p;
    if (c == '&' || c == '<' || c == '>' || c == '"' || c == '\'') {
      snprintf(out, out_sz, "%s", fallback);
      return;
    }
    if (!is_allowed_name_char(c)) {
      snprintf(out, out_sz, "%s", fallback);
      return;
    }
    out[len++] = (char)c;
  }

  while (len > 0 && out[len - 1] == ' ') len--;
  if (len == 0) snprintf(out, out_sz, "%s", fallback);
  else out[len] = '\0';
}

static void html_escape(const char *in, char *out, size_t out_sz) {
  size_t o = 0;
  for (size_t i = 0; in[i] != '\0' && o + 1 < out_sz; i++) {
    char c = in[i];
    const char *rep = NULL;
    switch (c) {
      case '&': rep = "&amp;"; break;
      case '<': rep = "&lt;"; break;
      case '>': rep = "&gt;"; break;
      case '"': rep = "&quot;"; break;
      case '\'': rep = "&#39;"; break;
      default: break;
    }
    if (rep) {
      size_t rlen = strlen(rep);
      if (o + rlen >= out_sz) break;
      memcpy(out + o, rep, rlen);
      o += rlen;
    } else {
      out[o++] = c;
    }
  }
  out[o] = '\0';
}

static const char *get_query_param(const char *qs, const char *key, char *buf, size_t buf_sz) {
  if (!qs || !key) return NULL;
  size_t klen = strlen(key);

  const char *p = qs;
  while (*p) {
    if (strncmp(p, key, klen) == 0 && p[klen] == '=') {
      p += klen + 1;
      size_t i = 0;
      while (*p && *p != '&' && i + 1 < buf_sz) {
        buf[i++] = *p++;
      }
      buf[i] = '\0';
      return buf;
    }
    while (*p && *p != '&') p++;
    if (*p == '&') p++;
  }
  return NULL;
}

static Response render_response_from_env(const Env *env, const Request *req) {
  Response resp;
  resp.status = 200;
  resp.content_type = "text/html; charset=utf-8";
  resp.body = NULL;

  // Fixed/allowlisted template (not user-controlled)
  const char *tpl = env->home_template;

  char raw_name[256];
  char name[128];
  char esc_name[256];

  raw_name[0] = '\0';
  get_query_param(req->query_string, "name", raw_name, sizeof(raw_name));
  safe_name(raw_name, name, sizeof(name));

  // Output-encode before insertion
  html_escape(name, esc_name, sizeof(esc_name));

  const char *ph = "{{name}}";
  const char *pos = strstr(tpl, ph);

  if (!pos) {
    resp.body = strdup(tpl);
    return resp;
  }

  size_t pre_len = (size_t)(pos - tpl);
  size_t post_len = strlen(pos + strlen(ph));
  size_t total = pre_len + strlen(esc_name) + post_len;

  resp.body = (char *)malloc(total + 1);
  if (!resp.body) {
    resp.status = 500;
    resp.content_type = "text/plain; charset=utf-8";
    resp.body = strdup("Internal Server Error");
    return resp;
  }

  memcpy(resp.body, tpl, pre_len);
  memcpy(resp.body + pre_len, esc_name, strlen(esc_name));
  memcpy(resp.body + pre_len + strlen(esc_name), pos + strlen(ph), post_len);
  resp.body[total] = '\0';

  return resp;
}

static Response home(const Env *env, const Request *req) {
  return render_response_from_env(env, req);
}

int main(void) {
  Env env = {
    .home_template =
      "<!doctype html><html><head><meta charset=\"utf-8\"><title>Home</title></head>"
      "<body><h1>Welcome, {{name}}!</h1></body></html>"
  };

  Request req = { .query_string = "name=Alice" };
  Response resp = home(&env, &req);

  printf("Status: %d\nContent-Type: %s\n\n%s\n", resp.status, resp.content_type, resp.body);

  free(resp.body);
  return 0;
}