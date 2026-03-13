// C (SQLite + execve) - secure email check + safe PHP invocation
// Compile: cc unsub.c -lsqlite3 -o unsub
#define _POSIX_C_SOURCE 200809L
#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sqlite3.h>

static int is_valid_email_basic(const char *s) {
    if (!s) return 0;
    size_t n = strlen(s);
    if (n < 3 || n > 254) return 0;

    int at = -1;
    for (size_t i = 0; i < n; i++) {
        unsigned char c = (unsigned char)s[i];
        if (c <= 32 || c >= 127) return 0;            // no spaces/control/non-ascii
        if (c == '@') { if (at != -1) return 0; at = (int)i; }
    }
    if (at <= 0 || (size_t)at >= n - 2) return 0;     // must have local + domain
    // domain must contain a dot not at edges
    const char *domain = s + at + 1;
    const char *dot = strchr(domain, '.');
    if (!dot || dot == domain || dot[1] == '\0') return 0;
    return 1;
}

static int normalize_email_lowercase(const char *in, char *out, size_t out_sz) {
    if (!in || !out || out_sz == 0) return 0;
    // trim leading/trailing spaces
    const char *start = in;
    while (*start && isspace((unsigned char)*start)) start++;
    const char *end = in + strlen(in);
    while (end > start && isspace((unsigned char)end[-1])) end--;
    size_t len = (size_t)(end - start);
    if (len == 0 || len >= out_sz) return 0;

    for (size_t i = 0; i < len; i++) {
        unsigned char c = (unsigned char)start[i];
        if (c >= 127) return 0; // reject non-ascii
        out[i] = (char)tolower(c);
    }
    out[len] = '\0';
    return 1;
}

static int email_exists_sqlite(sqlite3 *db, const char *email_norm) {
    const char *sql = "SELECT 1 FROM users WHERE email = ?1 LIMIT 1;";
    sqlite3_stmt *stmt = NULL;
    int exists = 0;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) return 0;
    if (sqlite3_bind_text(stmt, 1, email_norm, -1, SQLITE_TRANSIENT) != SQLITE_OK) {
        sqlite3_finalize(stmt);
        return 0;
    }
    int rc = sqlite3_step(stmt);
    if (rc == SQLITE_ROW) exists = 1;
    sqlite3_finalize(stmt);
    return exists;
}

static int run_php_unsubscribe(const char *php_bin, const char *script_path, const char *email_norm, int timeout_sec) {
    int pipefd[2];
    if (pipe(pipefd) != 0) return -1;

    pid_t pid = fork();
    if (pid < 0) {
        close(pipefd[0]); close(pipefd[1]);
        return -1;
    }

    if (pid == 0) {
        // child: stdout -> pipe write end
        close(pipefd[0]);
        if (dup2(pipefd[1], STDOUT_FILENO) < 0) _exit(127);
        close(pipefd[1]);

        // Fixed paths, no shell, argv safe
        char *const argv[] = {(char *)php_bin, (char *)script_path, (char *)email_norm, NULL};
        execve(php_bin, argv, (char *const[]){ NULL });
        _exit(127);
    }

    // parent
    close(pipefd[1]);

    int status = 0;
    int waited = 0;
    while (1) {
        pid_t w = waitpid(pid, &status, WNOHANG);
        if (w == pid) break;
        if (w < 0) { close(pipefd[0]); return -1; }
        if (waited >= timeout_sec) {
            kill(pid, SIGKILL);
            waitpid(pid, &status, 0);
            close(pipefd[0]);
            return -1;
        }
        sleep(1);
        waited++;
    }

    // Read first line of output (optional). We rely on exit code primarily.
    char buf[64];
    ssize_t r = read(pipefd[0], buf, sizeof(buf) - 1);
    close(pipefd[0]);
    if (r > 0) { buf[r] = '\0'; }

    if (WIFEXITED(status)) return WEXITSTATUS(status);
    return -1;
}

// Returns: 0 if email not found OR unsubscribe failed; 1 if email found AND PHP returned 0.
int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "usage: %s <db_path> <email>\n", argv[0]);
        return 2;
    }

    const char *db_path = argv[1];
    const char *email_in = argv[2];

    char email_norm[256];
    if (!normalize_email_lowercase(email_in, email_norm, sizeof(email_norm))) return 0;
    if (!is_valid_email_basic(email_norm)) return 0;

    sqlite3 *db = NULL;
    if (sqlite3_open_v2(db_path, &db, SQLITE_OPEN_READONLY, NULL) != SQLITE_OK) {
        if (db) sqlite3_close(db);
        return 0;
    }

    int exists = email_exists_sqlite(db, email_norm);
    sqlite3_close(db);
    if (!exists) return 0;

    const char *php_bin = "/usr/bin/php";
    const char *script_path = "/opt/unsub/unsubscribe.php";
    int php_rc = run_php_unsubscribe(php_bin, script_path, email_norm, 5);

    if (php_rc == 0) return 1;
    return 0;
}