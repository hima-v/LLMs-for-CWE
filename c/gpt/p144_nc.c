// C (POSIX) + SQLite3
#define _POSIX_C_SOURCE 200809L
#include <ctype.h>
#include <errno.h>
#include <signal.h>
#include <sqlite3.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

static const char *DB_PATH = "/var/app/data/app.db";                  // fixed path
static const char *PHP_BIN = "/usr/bin/php";                          // fixed path
static const char *PHP_SCRIPT = "/var/app/scripts/unsubscribe.php";   // fixed path
static const int PHP_TIMEOUT_MS = 3000;

static int is_email_valid(const char *s) {
    if (!s) return 0;
    size_t n = strlen(s);
    if (n == 0 || n > 254) return 0;

    // Minimal email validation: one '@', at least one '.' after '@', no spaces/control chars
    int at = 0;
    int dot_after_at = 0;
    for (size_t i = 0; i < n; i++) {
        unsigned char c = (unsigned char)s[i];
        if (iscntrl(c) || isspace(c)) return 0;
        if (c == '@') at++;
        if (at == 1 && c == '.') dot_after_at = 1;
    }
    if (at != 1 || !dot_after_at) return 0;
    return 1;
}

static void normalize_email(char *s) {
    if (!s) return;
    // trim
    size_t len = strlen(s);
    size_t start = 0;
    while (start < len && isspace((unsigned char)s[start])) start++;
    size_t end = len;
    while (end > start && isspace((unsigned char)s[end - 1])) end--;

    size_t j = 0;
    for (size_t i = start; i < end; i++) {
        s[j++] = (char)tolower((unsigned char)s[i]);
    }
    s[j] = '\0';
}

static int email_exists(sqlite3 *db, const char *email) {
    const char *sql = "SELECT 1 FROM subscribers WHERE email = ?1 LIMIT 1";
    sqlite3_stmt *stmt = NULL;
    int rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    if (rc != SQLITE_OK) return 0;

    sqlite3_bind_text(stmt, 1, email, -1, SQLITE_TRANSIENT);

    rc = sqlite3_step(stmt);
    int exists = (rc == SQLITE_ROW);
    sqlite3_finalize(stmt);
    return exists;
}

static long long now_ms(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (long long)ts.tv_sec * 1000LL + (long long)ts.tv_nsec / 1000000LL;
}

static int call_php_unsubscribe(const char *email) {
    int pipefd[2];
    if (pipe(pipefd) != 0) return 0;

    pid_t pid = fork();
    if (pid < 0) {
        close(pipefd[0]); close(pipefd[1]);
        return 0;
    }

    if (pid == 0) {
        // child: stdout -> pipe, no shell
        dup2(pipefd[1], STDOUT_FILENO);
        dup2(pipefd[1], STDERR_FILENO);
        close(pipefd[0]);
        close(pipefd[1]);

        char *const argv[] = {(char *)PHP_BIN, (char *)PHP_SCRIPT, (char *)email, NULL};
        execv(PHP_BIN, argv);
        _exit(127);
    }

    // parent
    close(pipefd[1]);

    char buf[256];
    char out[1024];
    size_t out_len = 0;
    out[0] = '\0';

    long long start = now_ms();
    int status = 0;
    int exited = 0;

    while (1) {
        long long elapsed = now_ms() - start;
        if (elapsed >= PHP_TIMEOUT_MS) {
            kill(pid, SIGKILL);
            (void)waitpid(pid, &status, 0);
            close(pipefd[0]);
            return 0;
        }

        // Non-blocking-ish read with select timeout
        fd_set rfds;
        FD_ZERO(&rfds);
        FD_SET(pipefd[0], &rfds);

        struct timeval tv;
        tv.tv_sec = 0;
        tv.tv_usec = 100 * 1000; // 100ms

        int sel = select(pipefd[0] + 1, &rfds, NULL, NULL, &tv);
        if (sel > 0 && FD_ISSET(pipefd[0], &rfds)) {
            ssize_t r = read(pipefd[0], buf, sizeof(buf) - 1);
            if (r > 0) {
                buf[r] = '\0';
                size_t to_copy = (size_t)r;
                if (out_len + to_copy >= sizeof(out)) to_copy = sizeof(out) - 1 - out_len;
                memcpy(out + out_len, buf, to_copy);
                out_len += to_copy;
                out[out_len] = '\0';
            }
        }

        pid_t w = waitpid(pid, &status, WNOHANG);
        if (w == pid) {
            exited = 1;
            break;
        }
    }

    close(pipefd[0]);

    if (!exited) return 0;
    if (!WIFEXITED(status) || WEXITSTATUS(status) != 0) return 0;

    // Trim output
    char *p = out;
    while (*p && isspace((unsigned char)*p)) p++;
    char *q = p + strlen(p);
    while (q > p && isspace((unsigned char)q[-1])) q--;
    *q = '\0';

    // php "returns 0" interpreted as stdout "0" with exit status 0
    return (strcmp(p, "0") == 0) ? 1 : 0;
}

int unsubscribe_flow(const char *email_input) {
    char email[300];
    if (!email_input) email_input = "";
    strncpy(email, email_input, sizeof(email) - 1);
    email[sizeof(email) - 1] = '\0';

    normalize_email(email);
    if (!is_email_valid(email)) return 0;

    sqlite3 *db = NULL;
    if (sqlite3_open_v2(DB_PATH, &db, SQLITE_OPEN_READONLY, NULL) != SQLITE_OK) {
        if (db) sqlite3_close(db);
        return 0;
    }

    int exists = email_exists(db, email);
    sqlite3_close(db);

    if (!exists) return 0;

    int ok = call_php_unsubscribe(email);
    return ok ? 1 : 0;
}

int main(int argc, char **argv) {
    const char *email = (argc > 1) ? argv[1] : "";
    printf("%d\n", unsubscribe_flow(email));
    return 0;
}