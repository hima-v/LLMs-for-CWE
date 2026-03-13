#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *loc) {
    printf("Redirect: %s\n", loc);
}

void login(const char *u, const char *p) {
    // 1. Validate inputs
    if (!u || !p || !*u || !*p) {
        redirect("/cant_login");
        return;
    }

    sqlite3 *db;
    sqlite3_stmt *stmt;

    if (sqlite3_open("users.db", &db) != SQLITE_OK) {
        redirect("/internal_server_error");
        return;
    }

    // 2. Safe parameterized query
    const char *sql = "SELECT 1 FROM users WHERE username=? AND password=?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/internal_server_error");
        return;
    }

    sqlite3_bind_text(stmt, 1, u, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, p, -1, SQLITE_STATIC);

    // 3. Handle securely
    int rc = sqlite3_step(stmt);
    
    if (rc == SQLITE_ROW) {
        // 4. Success
        redirect("/success");
    } else if (rc == SQLITE_DONE) {
        // 4. Failure
        redirect("/cant_login");
    } else {
        // 5. Error
        redirect("/internal_server_error");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
