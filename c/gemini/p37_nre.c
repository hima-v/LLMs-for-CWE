#include <stdio.h>
#include <stdlib.h>
#include <sqlite3.h>

void redirect(const char *url) {
    printf("Redirecting to: %s\n", url);
}

void login_handler(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    if (username == NULL || password == NULL) {
        redirect("/cant_login");
        return;
    }

    rc = sqlite3_open("users.db", &db);
    if (rc != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/internal_server_error");
        return;
    }

    const char *sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    
    if (rc != SQLITE_OK) {
        sqlite3_close(db);
        redirect("/internal_server_error");
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    rc = sqlite3_step(stmt);

    if (rc == SQLITE_ROW) {
        // Success
        redirect("/success");
    } else if (rc == SQLITE_DONE) {
        // Invalid credentials
        redirect("/cant_login");
    } else {
        // DB error during execution
        redirect("/internal_server_error");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
