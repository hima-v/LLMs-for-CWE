#include <mysql/mysql.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void normalize_username(const char *in, char *out, size_t out_sz) {
    if (!in || out_sz == 0) { if (out_sz) out[0] = '\0'; return; }

    const unsigned char *p = (const unsigned char *)in;
    while (*p && isspace(*p)) p++;

    size_t len = 0;
    while (*p && len + 1 < out_sz) {
        unsigned char c = *p++;
        if (c >= 'A' && c <= 'Z') c = (unsigned char)(c - 'A' + 'a');
        out[len++] = (char)c;
    }
    while (len > 0 && isspace((unsigned char)out[len - 1])) len--;
    out[len] = '\0';
}

static int username_exists(const char *username) {
    char uname[256];
    normalize_username(username, uname, sizeof(uname));
    if (uname[0] == '\0') return 0;

    const char *host = getenv("DB_HOST"); if (!host) host = "localhost";
    const char *port_s = getenv("DB_PORT"); if (!port_s) port_s = "3306";
    const char *db = getenv("DB_NAME"); if (!db) db = "appdb";
    const char *user = getenv("DB_USER"); if (!user) user = "app_readonly";
    const char *pass = getenv("DB_PASSWORD"); if (!pass) pass = "";

    unsigned int port = (unsigned int)strtoul(port_s, NULL, 10);

    MYSQL *conn = mysql_init(NULL);
    if (!conn) return 0;
    if (!mysql_real_connect(conn, host, user, pass, db, port, NULL, 0)) {
        mysql_close(conn);
        return 0;
    }

    const char *q = "SELECT 1 FROM users WHERE username_normalized = ? LIMIT 1";
    MYSQL_STMT *stmt = mysql_stmt_init(conn);
    if (!stmt) { mysql_close(conn); return 0; }
    if (mysql_stmt_prepare(stmt, q, (unsigned long)strlen(q)) != 0) {
        mysql_stmt_close(stmt); mysql_close(conn); return 0;
    }

    MYSQL_BIND params[1];
    memset(params, 0, sizeof(params));
    unsigned long uname_len = (unsigned long)strlen(uname);
    params[0].buffer_type = MYSQL_TYPE_STRING;
    params[0].buffer = (void *)uname;
    params[0].buffer_length = (unsigned long)sizeof(uname);
    params[0].length = &uname_len;

    if (mysql_stmt_bind_param(stmt, params) != 0) {
        mysql_stmt_close(stmt); mysql_close(conn); return 0;
    }
    if (mysql_stmt_execute(stmt) != 0) {
        mysql_stmt_close(stmt); mysql_close(conn); return 0;
    }

    int exists = 0;
    if (mysql_stmt_store_result(stmt) == 0) {
        int rc = mysql_stmt_fetch(stmt);
        if (rc == 0 || rc == MYSQL_DATA_TRUNCATED) exists = 1;
    }

    mysql_stmt_free_result(stmt);
    mysql_stmt_close(stmt);
    mysql_close(conn);
    return exists;
}

int main(int argc, char **argv) {
    if (argc != 2) { puts("false"); return 0; }
    puts(username_exists(argv[1]) ? "true" : "false");
    return 0;
}