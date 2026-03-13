/* C (PostgreSQL libpq) — parameterized query, normalization, no raw SQL errors */
#include <stdbool.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <libpq-fe.h>

/* NOTE: This normalization is ASCII-focused (trim + lowercase).
   For full Unicode normalization/casefold, normalize at ingestion and store username_norm in DB. */
static void normalize_username_ascii(const char *in, char *out, size_t out_sz) {
    if (!in || out_sz == 0) { if (out_sz) out[0] = '\0'; return; }

    const char *start = in;
    while (*start && isspace((unsigned char)*start)) start++;

    const char *end = in + strlen(in);
    while (end > start && isspace((unsigned char)*(end - 1))) end--;

    size_t len = (size_t)(end - start);
    if (len >= out_sz) len = out_sz - 1;

    for (size_t i = 0; i < len; i++) {
        unsigned char c = (unsigned char)start[i];
        out[i] = (char)tolower(c);
    }
    out[len] = '\0';
}

/* Returns true if exists, false otherwise (including on DB error, without exposing details). */
bool username_exists(PGconn *conn, const char *username_raw) {
    if (!conn || PQstatus(conn) != CONNECTION_OK || !username_raw) return false;

    char uname[65];
    normalize_username_ascii(username_raw, uname, sizeof(uname));
    if (uname[0] == '\0') return false;

    const char *paramValues[1] = { uname };
    int paramLengths[1] = { (int)strlen(uname) };
    int paramFormats[1] = { 0 }; /* text */

    /* Parameterized query prevents SQL injection (CWE-89). */
    PGresult *res = PQexecParams(
        conn,
        "SELECT 1 FROM users WHERE username_norm = $1 LIMIT 1",
        1,
        NULL,
        paramValues,
        paramLengths,
        paramFormats,
        0
    );

    if (!res) {
        /* Do not log/return raw SQL errors (CWE-209). */
        return false;
    }

    ExecStatusType st = PQresultStatus(res);
    bool exists = false;

    if (st == PGRES_TUPLES_OK) {
        exists = (PQntuples(res) > 0);
    } else {
        /* Do not expose PQerrorMessage(conn). */
        exists = false;
    }

    PQclear(res);
    return exists;
}

/*
Least-privilege reminder:
Use a DB role that only has SELECT on users(username_norm) (and no write permissions).
Connect with that role in your connection string / environment (not shown here).
*/