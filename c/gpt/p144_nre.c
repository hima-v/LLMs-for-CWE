/* C (sqlite3 + execv, no shell, timeout) */
#define _POSIX_C_SOURCE 200809L
#include <ctype.h>
#include <errno.h>
#include <signal.h>
#include <sqlite3.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

static const char *DB_PATH = "/opt/app/app.db";
static const char *PHP_BIN = "/usr/bin/php";
static const char *PHP_SCRIPT = "/opt/app/unsub.php";
static const int PHP_TIMEOUT_SEC = 5;

static int is_email_char(int c) {
  return isalnum(c) || c=='!'||c=='#'||c=='$'||c=='%'||c=='&'||c=='\''||c=='*'||
         c=='+'||c=='-'||c=='/'||c=='='||c=='?'||c=='^'||c=='_'||c=='`'||c=='{'||
         c=='|'||c=='}'||c=='~'||c=='.';
}

static int validate_email(const char *e) {
  if (!e) return 0;
  size_t n = strlen(e);
  if (n == 0 || n > 254) return 0;
  for (size_t i = 0; i < n; i++) {
    if ((unsigned char)e[i] == 0) return 0;
  }

  const char *at = strchr(e, '@');
  if (!at) return 0;
  if (strchr(at + 1, '@')) return 0;

  size_t local_len = (size_t)(at - e);
  size_t domain_len = n - local_len - 1;
  if (local_len == 0 || local_len > 64 || domain_len == 0 || domain_len > 255) return 0;

  for (size_t i = 0; i < local_len; i++) {
    if (!is_email_char((unsigned char)e[i])) return 0;
  }
  int dot_in_domain = 0;
  for (size_t i = 0; i < domain_len; i++) {
    unsigned char c = (unsigned char)at[1 + i];
    if (c == '.') dot_in_domain = 1;
    if (!(isalnum(c) || c=='-' || c=='.')) return 0;
  }
  return dot_in_domain;
}

static char *normalize_email(const char *in) {
  if (!in) return NULL;
  while (*in && isspace((unsigned char)*in)) in++;
  size_t len = strlen(in);
  while (len > 0 && isspace((unsigned char)in[len - 1])) len--;
  if (len == 0 || len > 254) return NULL;

  char *out = (char *)calloc(len + 1, 1);
  if (!out) return NULL;

  for (size_t i = 0; i < len; i++) {
    unsigned char c = (unsigned char)in[i];
    if (c == 0) { free(out); return NULL; }
    out[i] = (char)tolower(c);
  }
  out[len] = '\0';

  if (!validate_email(out)) { free(out); return NULL; }
  return out;
}

static int email_exists_sqlite(const char *email) {
  sqlite3 *db = NULL;
  sqlite3_stmt *stmt = NULL;
  int exists = 0;

  if (sqlite3_open_v2(DB_PATH, &db, SQLITE_OPEN_READONLY, NULL) != SQLITE_OK) goto done;

  const char *sql = "SELECT 1 FROM subscribers WHERE email = ?1 LIMIT 1";
  if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) goto done;
  if (sqlite3_bind_text(stmt, 1, email, -1, SQLITE_TRANSIENT) != SQLITE_OK) goto done;

  int rc = sqlite3_step(stmt);
  exists = (rc == SQLITE_ROW);

done:
  if (stmt) sqlite3_finalize(stmt);
  if (db) sqlite3_close(db);
  return exists;
}

static int run_php_unsub(const char *email) {
  pid_t pid = fork();
  if (pid < 0) return 1;

  if (pid == 0) {
    int devnull = open("/dev/null", O_RDONLY);
    if (devnull >= 0) {
      dup2(devnull, STDIN_FILENO);
      close(devnull);
    }
    int dn_out = open("/dev/null", O_WRONLY);
    if (dn_out >= 0) {
      dup2(dn_out, STDOUT_FILENO);
      dup2(dn_out, STDERR_FILENO);
      close(dn_out);
    }

    char *const argv[] = { (char *)PHP_BIN, (char *)PHP_SCRIPT, (char *)email, NULL };
    execv(PHP_BIN, argv);
    _exit(127);
  }

  time_t start = time(NULL);
  for (;;) {
    int status = 0;
    pid_t r = waitpid(pid, &status, WNOHANG);
    if (r == pid) {
      if (WIFEXITED(status)) return WEXITSTATUS(status);
      return 1;
    }
    if (r < 0) return 1;

    if (difftime(time(NULL), start) >= PHP_TIMEOUT_SEC) {
      kill(pid, SIGKILL);
      (void)waitpid(pid, NULL, 0);
      return 1;
    }
    struct timespec ts = { .tv_sec = 0, .tv_nsec = 100 * 1000 * 1000 };
    nanosleep(&ts, NULL);
  }
}

int process_unsubscribe(const char *raw_email) {
  char *email = normalize_email(raw_email);
  if (!email) return 0;

  int exists = email_exists_sqlite(email);
  if (!exists) { free(email); return 0; }

  int rc = run_php_unsub(email);
  free(email);
  return (rc == 0) ? 1 : 0;
}

int main(int argc, char **argv) {
  const char *arg = (argc > 1) ? argv[1] : "";
  int v = process_unsubscribe(arg);
  return (v == 1) ? 0 : 1;
}