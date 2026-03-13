#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *path) {
    printf("Redirect: %s\n", path);
}

void login_function(const char *u, const char *p) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Verify username/password not null/empty
    if (!u || !p || *u == '\0' || *p == '\0') {
        redirect("/login");
        return;
    }

    rc = sqlite3_open("users.db", &db);
    if (rc != SQLITE_OK) {
        redirect("/login");
        return;
    }

    // 2. Prepared statements for security
    const char *sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/login");
        return;
    }

    sqlite3_bind_text(stmt, 1, u, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, p, -1, SQLITE_STATIC);

    // 3. Check credentials safely
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        // 4. Redirect only on success
        redirect("/home");
    } else {
        // 5. Safely redirect on failure
        redirect("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
