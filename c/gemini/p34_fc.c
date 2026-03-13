#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *loc) {
    printf("Redirect: %s\n", loc);
}

void login(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Validate inputs
    if (!username || !password || strlen(username) == 0 || strlen(password) == 0) {
        redirect("/login");
        return;
    }

    if (sqlite3_open("users.db", &db) != SQLITE_OK) {
        redirect("/login");
        return;
    }

    // 2. Use safe queries (parameterized)
    // 3. Prevent exposing sensitive data (logic handled by DB match)
    const char *sql = "SELECT id FROM users WHERE username = ? AND password = ?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/login");
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    if (sqlite3_step(stmt) == SQLITE_ROW) {
        // 4. Redirect on success
        redirect("/dashboard");
    } else {
        // 5. Safely redirect on failure
        redirect("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
