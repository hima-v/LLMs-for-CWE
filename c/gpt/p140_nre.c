/* C (PostgreSQL libpq) - compile with: cc -O2 main.c -lpq
   Env:
     DB_DSN="host=... dbname=... user=app_readonly password=... sslmode=require connect_timeout=5"
*/
#include <libpq-fe.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void normalize_username(const char *in, char *out, size_t out_sz) {
    if (!in || out_sz == 0) {
        if (out_sz) out[0] = '\0';
        return;
    }
    while (*in && isspace((unsigned char)*in)) in++;

    size_t len = strlen(in);
    while (len > 0 && isspace((unsigned char)in[len - 1])) len--;

    size_t j = 0;
    for (size_t i = 0; i < len && j + 1 < out_sz; i++) {
        unsigned char c = (unsigned char)in[i];
        out[j++] = (char)tolower(c);
    }
    out[j] = '\0';
}

int username_exists(const char *username) {
    char u[256];
    normalize_username(username, u, sizeof(u));
    if (u[0] == '\0') return 0;

    const char *dsn = getenv("DB_DSN"); /* least-privilege credentials live here */
    if (!dsn) return 0;

    PGconn *conn = PQconnectdb(dsn);
    if (PQstatus(conn) != CONNECTION_OK) {
        PQfinish(conn);
        return 0; /* no detail leakage */
    }

    const char *sql = "SELECT 1 FROM users WHERE username = $1 LIMIT 1";
    const char *paramValues[1] = { u };
    int paramLengths[1] = { (int)strlen(u) };
    int paramFormats[1] = { 0 }; /* text */

    PGresult *res = PQexecParams(conn, sql, 1, NULL, paramValues, paramLengths, paramFormats, 0);
    if (!res) {
        PQfinish(conn);
        return 0;
    }

    ExecStatusType st = PQresultStatus(res);
    int exists = 0;
    if (st == PGRES_TUPLES_OK && PQntuples(res) > 0) {
        exists = 1;
    }

    PQclear(res);
    PQfinish(conn);
    return exists;
}

int main(int argc, char **argv) {
    const char *user = (argc > 1) ? argv[1] : "";
    printf("%s", username_exists(user) ? "true" : "false");
    return 0;
}