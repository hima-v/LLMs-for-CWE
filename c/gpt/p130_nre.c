/* c (minimal HTTP response; fixed template; validate + HTML-escape) */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

static int is_allowed_char(unsigned char c) {
  return isalnum(c) || c == ' ' || c == '_' || c == '.' || c == '-' ;
}

static void safe_name(const char *raw, char *out, size_t out_sz) {
  const char *def = "Guest";
  if (!raw) {
    snprintf(out, out_sz, "%s", def);
    return;
  }

  while (*raw && isspace((unsigned char)*raw)) raw++;

  size_t len = 0;
  for (const char *p = raw; *p && len < 64; p++) {
    unsigned char c = (unsigned char)*p;
    if (c == '&') break; /* stop at query separator if caller passes full query */
    if (!is_allowed_char(c)) {
      snprintf(out, out_sz, "%s", def);
      return;
    }
    out[len++] = (char)c;
  }

  while (len > 0 && isspace((unsigned char)out[len - 1])) len--;
  out[len] = '\0';

  if (len == 0) snprintf(out, out_sz, "%s", def);
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
      case '\'': rep = "&#x27;"; break;
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

static const char *HOME_TEMPLATE =
  "<!doctype html><html><head><meta charset=\"utf-8\"><title>Home</title></head>"
  "<body><h1>Welcome, %s!</h1></body></html>";

static void render_response_from_env(const char *raw_name) {
  char name[128];
  char escaped[512];

  safe_name(raw_name, name, sizeof(name));
  html_escape(name, escaped, sizeof(escaped));

  char body[2048];
  snprintf(body, sizeof(body), HOME_TEMPLATE, escaped);

  printf("HTTP/1.1 200 OK\r\n");
  printf("Content-Type: text/html; charset=utf-8\r\n");
  printf("Content-Length: %zu\r\n", strlen(body));
  printf("X-Content-Type-Options: nosniff\r\n");
  printf("\r\n");
  fwrite(body, 1, strlen(body), stdout);
}

static const char *get_query_param_name(const char *query) {
  if (!query) return NULL;
  const char *p = strstr(query, "name=");
  if (!p) return NULL;
  p += 5;
  return p;
}

static void home(const char *query_string) {
  const char *raw_name = get_query_param_name(query_string);
  render_response_from_env(raw_name);
}

int main(int argc, char **argv) {
  const char *query = (argc > 1) ? argv[1] : NULL; /* e.g., "name=Alice" */
  home(query);
  return 0;
}