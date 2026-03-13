/*
  Compile (example, PostgreSQL libpq): gcc check_user_exists.c -lpq -o check_user_exists
  Note: Set DB_CONNINFO via environment variable (recommended).
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <libpq-fe.h>

static void normalize_username(const char *in, char *out, size_t out_sz) {
    size_t j = 0;
    // Trim leading whitespace
    while (*in && isspace((unsigned char)*in)) in++;

    // Copy with safe normalization: lowercase ASCII, collapse internal whitespace to single space,
    // and trim trailing whitespace.
    bool prev_space = false;
    for (; *in && j + 1 < out_sz; in++) {
        unsigned char c = (unsigned char)*in;

        if (isspace(c)) {
            if (!prev_space) {
                out[j++] = ' ';
                prev_space = true;
            }
            continue;
        }

        prev_space = false;

        if (c < 128) {
            out[j++] = (char)tolower(c);
        } else {
            // Keep non-ASCII bytes as-is (avoid locale-dependent transforms)
            out[j++] = (char)c;
        }
    }

    // Trim trailing space
    while (j > 0 && out[j - 1] == ' ') j--;
    out[j] = '\0';
}

bool username_exists_pg(const char *conninfo, const char *username) {
    PGconn *conn = PQconnectdb(conninfo);
    if (PQstatus(conn) != CONNECTION_OK) {
        // Avoid leaking connection details
        PQfinish(conn);
        return false;
    }

    char norm[256];
    normalize_username(username, norm, sizeof(norm));
    if (norm[0] == '\0') {
        PQfinish(conn);
        return false;
    }

    // Parameterized query
    const char *sql = "SELECT 1 FROM users WHERE username = $1 LIMIT 1";
    const char *paramValues[1] = { norm };

    PGresult *res = PQexecParams(
        conn,
        sql,
        1,
        NULL,            // let the server infer types
        paramValues,
        NULL,            // param lengths
        NULL,            // param formats (all text)
        0                // text results
    );

    bool exists = false;
    if (res && PQresultStatus(res) == PGRES_TUPLES_OK) {
        exists = (PQntuples(res) > 0);
    }

    if (res) PQclear(res);
    PQfinish(conn);
    return exists;
}

int main(int argc, char **argv) {
    const char *conninfo = getenv("DB_CONNINFO");
    if (!conninfo || conninfo[0] == '\0' || argc < 2) {
        // Minimal message, no sensitive info
        return 1;
    }

    bool ok = username_exists_pg(conninfo, argv[1]);
    // Exit code can indicate true/false without printing sensitive details
    return ok ? 0 : 2;
}