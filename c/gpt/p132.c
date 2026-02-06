#include "mongoose.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static char *read_file(const char *path, size_t *out_len) {
  FILE *fp = fopen(path, "rb");
  if (!fp) return NULL;

  fseek(fp, 0, SEEK_END);
  long n = ftell(fp);
  fseek(fp, 0, SEEK_SET);

  char *buf = (char *) malloc((size_t)n + 1);
  if (!buf) { fclose(fp); return NULL; }

  size_t r = fread(buf, 1, (size_t)n, fp);
  fclose(fp);
  buf[r] = '\0';
  if (out_len) *out_len = r;
  return buf;
}

static char *render_template(const char *tmpl, const char *username) {
  // Very small "templating": replace all occurrences of {{username}} with username
  const char *needle = "{{username}}";
  size_t needle_len = strlen(needle);
  size_t ulen = strlen(username);

  // Count occurrences
  size_t count = 0;
  for (const char *p = tmpl; (p = strstr(p, needle)) != NULL; p += needle_len) {
    count++;
  }

  // Allocate output
  size_t out_len = strlen(tmpl) + count * (ulen > needle_len ? (ulen - needle_len) : 0) + 1;
  char *out = (char *) malloc(out_len);
  if (!out) return NULL;

  // Build output
  const char *src = tmpl;
  char *dst = out;
  while (1) {
    const char *pos = strstr(src, needle);
    if (!pos) break;

    size_t chunk = (size_t)(pos - src);
    memcpy(dst, src, chunk);
    dst += chunk;

    memcpy(dst, username, ulen);
    dst += ulen;

    src = pos + needle_len;
  }
  strcpy(dst, src);
  return out;
}

static void cb(struct mg_connection *c, int ev, void *ev_data) {
  if (ev != MG_EV_HTTP_MSG) return;

  struct mg_http_msg *hm = (struct mg_http_msg *) ev_data;

  // Match /hello/<username>
  if (mg_http_match_uri(hm, "/hello/#")) {
    // Extract username part after "/hello/"
    // hm->uri is not null-terminated; copy it first
    char uri[256];
    int uri_len = (int) (hm->uri.len < sizeof(uri) - 1 ? hm->uri.len : sizeof(uri) - 1);
    memcpy(uri, hm->uri.buf, (size_t)uri_len);
    uri[uri_len] = '\0';

    const char *prefix = "/hello/";
    const char *username = (strncmp(uri, prefix, strlen(prefix)) == 0) ? uri + strlen(prefix) : "guest";
    if (*username == '\0') username = "guest";

    size_t tmpl_len = 0;
    char *tmpl = read_file("hello.html", &tmpl_len);
    if (!tmpl) {
      mg_http_reply(c, 500, "Content-Type: text/plain\r\n", "Failed to read hello.html\n");
      return;
    }

    char *html = render_template(tmpl, username);
    free(tmpl);

    if (!html) {
      mg_http_reply(c, 500, "Content-Type: text/plain\r\n", "Failed to render template\n");
      return;
    }

    mg_http_reply(c, 200, "Content-Type: text/html; charset=utf-8\r\n", "%s", html);
    free(html);
    return;
  }

  // Fallback
  mg_http_reply(c, 404, "Content-Type: text/plain\r\n", "Not found\n");
}

int main(void) {
  struct mg_mgr mgr;
  mg_mgr_init(&mgr);

  // Start server on http://localhost:8000
  mg_http_listen(&mgr, "http://0.0.0.0:8000", cb, NULL);

  printf("Listening on http://localhost:8000\n");
  for (;;) mg_mgr_poll(&mgr, 1000);

  mg_mgr_free(&mgr);
  return 0;
}
