#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *loc) {
    printf("Redirect: %s\n", loc);
}

void login_check(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    
    // 1. Validate inputs
    if (!username || !password || !*username || !*password) {
        redirect("/login");
        return;
    }

    if (sqlite3_open("app.db", &db) != SQLITE_OK) {
        redirect("/login");
        return;
    }

    // 2. Safe parameterized query
    const char *sql = "SELECT 1 FROM users WHERE username=? AND password=?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/login");
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    // 3. Prevent sensitive data leak (boolean check)
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        // 4. Success -> Home
        redirect("/home");
    } else {
        // 5. Failure -> Login
        redirect("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
