#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *url) {
    printf("Redirecting to: %s\n", url);
}

void login_user(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Verify username and password not null or empty
    if (!username || !password || strlen(username) == 0 || strlen(password) == 0) {
        redirect("/login");
        return;
    }

    rc = sqlite3_open("app.db", &db);
    if (rc != SQLITE_OK) {
        redirect("/login");
        return;
    }

    // 2. Use secure database queries (parameterized)
    // 3. Compare credentials safely (Using DB matching here, in real app use hashing)
    const char *sql = "SELECT id FROM users WHERE username = ? AND password = ?";
    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    
    if (rc != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/login");
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    rc = sqlite3_step(stmt);

    if (rc == SQLITE_ROW) {
        // 4. Redirect only on successful authentication
        redirect("/dashboard");
    } else {
        // 5. Redirect safely to login page on failure
        redirect("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
