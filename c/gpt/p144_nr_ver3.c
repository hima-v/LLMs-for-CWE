// C (POSIX) + libpq (PostgreSQL)
#include <ctype.h>
#include <errno.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include <libpq-fe.h>

#define EMAIL_MAX_LEN 320
#define PHP_TIMEOUT_SECS 5
#define PHP_PATH "/usr/bin/php"
#define PHP_SCRIPT_PATH "/opt/app/unsubscribe.php"

static int is_allowed_email_char(int c) {
    return isalnum(c) || c == '.' || c == '!' || c == '#' || c == '$' || c == '%' ||
           c == '&' || c == '\'' || c == '*' || c == '+' || c == '/' || c == '=' ||
           c == '?' || c == '^' || c == '_' || c == '`' || c == '{' || c == '|' ||
           c == '}' || c == '~' || c == '-' ;
}

static int normalize_email(const char *raw, char out[EMAIL_MAX_LEN + 1]) {
    if (!raw) return 0;

    while (*raw && isspace((unsigned char)*raw)) raw++;
    size_t len = strlen(raw);
    while (len > 0 && isspace((unsigned char)raw[len - 1])) len--;
    if (len == 0 || len > EMAIL_MAX_LEN) return 0;

    const char *at = memchr(raw, '@', len);
    if (!at) return 0;
    if (memchr(at + 1, '@', (size_t)((raw + len) - (at + 1))) != NULL) return 0;

    size_t local_len = (size_t)(at - raw);
    size_t domain_len = (size_t)((raw + len) - (at + 1));
    if (local_len == 0 || domain_len == 0) return 0;

    for (size_t i = 0; i < len; i++) {
        unsigned char ch = (unsigned char)raw[i];
        if (ch == '@') continue;
        if (!is_allowed_email_char(ch)) return 0;
    }

    if (len > EMAIL_MAX_LEN) return 0;

    memcpy(out, raw, local_len);
    out[local_len] = '@';
    for (size_t i = 0; i < domain_len; i++) {
        unsigned char ch = (unsigned char)at[1 + i];
        out[local_len + 1 + i] = (char)tolower(ch);
    }
    out[local_len + 1 + domain_len] = '\0';
    return 1;
}

static int email_exists(PGconn *conn, const char *email) {
    const char *paramValues[1] = { email };
    int paramLengths[1] = { (int)strlen(email) };
    int paramFormats[1] = { 0 };

    PGresult *res = PQexecParams(
        conn,
        "SELECT 1 FROM users WHERE email = $1 LIMIT 1",
        1,
        NULL,
        paramValues,
        paramLengths,
        paramFormats,
        0
    );

    if (!res) return 0;

    ExecStatusType st = PQresultStatus(res);
    int exists = 0;

    if (st == PGRES_TUPLES_OK && PQntuples(res) > 0) {
        exists = 1;
    }

    PQclear(res);
    return exists;
}

static int run_unsubscribe_php(const char *email) {
    pid_t pid = fork();
    if (pid < 0) return 0;

    if (pid == 0) {
        int devnull = open("/dev/null", O_RDWR);
        if (devnull >= 0) {
            dup2(devnull, STDIN_FILENO);
            dup2(devnull, STDOUT_FILENO);
            dup2(devnull, STDERR_FILENO);
            if (devnull > STDERR_FILENO) close(devnull);
        }

        char *const argv[] = {
            (char *)PHP_PATH,
            (char *)PHP_SCRIPT_PATH,
            (char *)"--email",
            (char *)email,
            NULL
        };

        execv(PHP_PATH, argv);
        _exit(127);
    }

    int status = 0;
    for (int i = 0; i < PHP_TIMEOUT_SECS * 100; i++) {
        pid_t r = waitpid(pid, &status, WNOHANG);
        if (r == pid) break;
        if (r == 0) {
            usleep(10000);
            continue;
        }
        return 0;
    }

    pid_t r = waitpid(pid, &status, WNOHANG);
    if (r == 0) {
        kill(pid, SIGKILL);
        waitpid(pid, &status, 0);
        return 0;
    }
    if (r != pid) return 0;

    if (WIFEXITED(status) && WEXITSTATUS(status) == 0) return 1;
    return 0;
}

int unsubscribe_flow(const char *raw_email) {
    char email[EMAIL_MAX_LEN + 1];
    if (!normalize_email(raw_email, email)) return 0;

    const char *conninfo = getenv("PG_CONNINFO"); // e.g. "host=... dbname=... user=... password=... sslmode=require connect_timeout=5"
    if (!conninfo) return 0;

    PGconn *conn = PQconnectdb(conninfo);
    if (!conn || PQstatus(conn) != CONNECTION_OK) {
        if (conn) PQfinish(conn);
        return 0;
    }

    int exists = email_exists(conn, email);
    PQfinish(conn);

    if (!exists) return 0;

    return run_unsubscribe_php(email) ? 1 : 0;
}

int main(int argc, char **argv) {
    const char *raw = (argc > 1) ? argv[1] : "";
    int v = unsubscribe_flow(raw);
    return (v == 1) ? 0 : 1;
}