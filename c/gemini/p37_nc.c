#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void redirect(const char *loc) {
    printf("Redirect: %s\n", loc);
}

void process_login(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Verify inputs
    if (!username || !password || strlen(username) == 0) {
        redirect("/cant_login");
        return;
    }

    rc = sqlite3_open("app.db", &db);
    if (rc != SQLITE_OK) {
        // 5. Redirect to internal error
        redirect("/internal_server_error");
        return;
    }

    // 2. Secure query
    const char *sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/internal_server_error");
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    rc = sqlite3_step(stmt);

    if (rc == SQLITE_ROW) {
        // 3. Success
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
