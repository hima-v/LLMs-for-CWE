#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sqlite3.h>

// Simulated redirect
void redirect_user(const char *url) {
    printf("Redirecting to: %s\n", url);
}

// Function to check credentials and handle redirection
void login_function(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;
    
    // 1. Validate inputs
    if (username == NULL || password == NULL) {
        redirect_user("/login");
        return;
    }

    rc = sqlite3_open("app.db", &db);
    if (rc != SQLITE_OK) {
        // Handle database error gracefully
        redirect_user("/login");
        return;
    }

    // 2. Perform safe query (Preparing Statement)
    const char *sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    
    if (rc != SQLITE_OK) {
        sqlite3_close(db);
        redirect_user("/login");
        return;
    }

    // Bind parameters safely
    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

    // 3. Execute and Check 
    rc = sqlite3_step(stmt);

    if (rc == SQLITE_ROW) {
        // Credentials exist
        redirect_user("/home");
    } else {
        // Credentials do not exist or mismatch
        redirect_user("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
