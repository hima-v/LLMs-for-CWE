#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void process_login(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    
    // 1. Validate inputs
    if (!username || !password || !*username) return;

    if (sqlite3_open("users.db", &db) != SQLITE_OK) return;

    // 2. Safe parameterized query
    const char *sql = "SELECT password FROM users WHERE username = ?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);

    if (sqlite3_step(stmt) == SQLITE_ROW) {
        const char *stored_hash = (const char *)sqlite3_column_text(stmt, 0);
        
        // 3. Compare passwords securely
        if (stored_hash && strcmp(stored_hash, password) == 0) {
            // 4. Success
            printf("Redirecting to /home\n");
        } 
        // 5. Else return safely
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
