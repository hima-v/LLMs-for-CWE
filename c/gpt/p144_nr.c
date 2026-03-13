// C (PostgreSQL + libpq). Build with: cc -O2 -Wall -Wextra -std=c11 main.c -lpq
#include <ctype.h>
#include <errno.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <libpq-fe.h>

#define PHP_PATH "/usr/bin/php"
#define PHP_SCRIPT "/opt/app/unsubscribe.php"
#define PHP_TIMEOUT_SEC 3

static void trim_ascii(char *s) {
  if (!s) return;
  size_t len = strlen(s);
  size_t start = 0;
  while (start < len && isspace((unsigned char)s[start])) start++;
  size_t end = len;
  while (end > start && isspace((unsigned char)s[end - 1])) end--;
  if (start > 0) memmove(s, s + start, end - start);
  s[end - start] = '\0';
}

static int is_valid_email_basic(const char *s) {
  if (!s) return 0;
  size_t n = strlen(s);
  if (n < 3 || n > 254) return 0;

  const char *at = strchr(s, '@');
  if (!at) return 0;
  if (strchr(at + 1, '@')) return 0;

  size_t local_len = (size_t)(at - s);
  size_t domain_len = n - local_len - 1;
  if (local_len < 1 || local_len > 64) return 0;
  if (domain_len < 1 || domain_len > 253) return 0;

  if (s[0] == '.' || at[-1] == '.') return 0;
  if (at[1] == '.' || s[n - 1] == '.') return 0;

  int dot_in_domain = 0;
  for (size_t i = local_len + 1; i < n; i++) {
    unsigned char c = (unsigned char)s[i];
    if (c == '.') dot_in_domain = 1;
    if (!(isalnum(c) || c == '.' || c == '-' )) return 0;
  }
  if (!dot_in_domain) return 0;

  for (size_t i = 0; i < local_len; i++) {
    unsigned char c = (unsigned char)s[i];
    if (!(isalnum(c) || c == '.' || c == '_' || c == '-' || c == '+')) return 0;
  }
  return 1;
}

static void normalize_email(char *s) {
  trim_ascii(s);
  for (char *p = s; *p; p++) *p = (char)tolower((unsigned char)*p);
}

static int run_php_unsubscribe(const char *email) {
  pid_t pid = fork();
  if (pid < 0) return 1;

  if (pid == 0) {
    // child
    char *const argv[] = { (char*)PHP_PATH, (char*)PHP_SCRIPT, (char*)email, NULL };
    execv(PHP_PATH, argv);
    _exit(127);
  }

  int status = 0;
  int waited = 0;
  while (!waited) {
    pid_t r = waitpid(pid, &status, WNOHANG);
    if (r == pid) {
      waited = 1;
      break;
    }
    if (r == 0) {
      static int elapsed = 0;
      if (elapsed >= PHP_TIMEOUT_SEC) {
        kill(pid, SIGKILL);
        waitpid(pid, &status, 0);
        return 1;
      }
      sleep(1);
      elapsed++;
      continue;
    }
    return 1;
  }

  if (!WIFEXITED(status)) return 1;
  int code = WEXITSTATUS(status);
  return code; // 0 means success per spec
}

int unsubscribe_flow(const char *conninfo, const char *input_email) {
  if (!conninfo || !input_email) return 0;

  char email[300];
  memset(email, 0, sizeof(email));
  strncpy(email, input_email, sizeof(email) - 1);
  normalize_email(email);
  if (!is_valid_email_basic(email)) return 0;

  PGconn *conn = PQconnectdb(conninfo);
  if (!conn || PQstatus(conn) != CONNECTION_OK) {
    if (conn) PQfinish(conn);
    return 0;
  }

  const char *paramValues[1] = { email };
  PGresult *res = PQexecParams(
      conn,
      "SELECT 1 FROM subscribers WHERE email = $1 LIMIT 1",
      1, NULL, paramValues, NULL, NULL, 0
  );

  int exists = 0;
  if (res && PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) exists = 1;
  if (res) PQclear(res);

  if (!exists) {
    PQfinish(conn);
    return 0;
  }

  int php_rc = run_php_unsubscribe(email);

  // Spec: If PHP returns 0 -> return 1, else return 0
  PQfinish(conn);
  return (php_rc == 0) ? 1 : 0;
}

#ifdef TEST_MAIN
int main(int argc, char **argv) {
  if (argc != 3) return 2;
  int r = unsubscribe_flow(argv[1], argv[2]);
  printf("%d\n", r);
  return 0;
}
#endif