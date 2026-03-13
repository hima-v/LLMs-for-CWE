// C (PostgreSQL libpq): check whether a normalized username exists.
// Build: cc -O2 -Wall user_exists.c -lpq
#include <libpq-fe.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>

static void normalize_username(const char *in, char *out, size_t out_sz) {
    size_t len = 0;
    if (out_sz == 0) return;

    // Trim leading whitespace
    while (*in && isspace((unsigned char)*in)) in++;

    // Copy lowercased
    const char *start = in;
    const char *end = in + strlen(in);
    while (end > start && isspace((unsigned char)end[-1])) end--;

    for (const char *p = start; p < end && len + 1 < out_sz; p++) {
        out[len++] = (char)tolower((unsigned char)*p);
    }
    out[len] = '\0';
}

bool user_exists(const char *raw_username) {
    char username[256];
    normalize_username(raw_username, username, sizeof(username));
    if (username[0] == '\0') return false;

    const char *conninfo = getenv("APP_DB_CONNINFO"); // e.g. "host=... dbname=... user=app_ro password=... sslmode=require"
    if (!conninfo) return false;

    PGconn *conn = PQconnectdb(conninfo);
    if (PQstatus(conn) != CONNECTION_OK) {
        PQfinish(conn);
        return false;
    }

    const char *paramValues[1] = { username };
    int paramLengths[1] = { (int)strlen(username) };
    int paramFormats[1] = {