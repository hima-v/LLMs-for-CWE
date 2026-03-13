#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

void safe_return() {
    printf("Return safely\n");
}

void redirect_home() {
    printf("Redirect: /home\n");
}

void login(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    
    // 1. Verify inputs
    if (!username || !password || strlen(username) == 0 || strlen(password) == 0) {
        safe_return();
        return;
    }

    if (sqlite3_open("users.db", &db) != SQLITE_OK) {
        safe_return();
        return;
    }

    // 2. Secure query (check username existence)
    const char *sql = "SELECT password FROM users WHERE username = ?";
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        safe_return();
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);

    if (sqlite3_step(stmt) == SQLITE_ROW) {
        const unsigned char *stored_pass = sqlite3_column_text(stmt, 0);
        
        // 3. Compare passwords securely (Basic strcmp here, ideally constant-time comparison)
        if (stored_pass && strcmp((const char*)stored_pass, password) == 0) {
            // 4. Redirect only if auth succeeds
            redirect_home();
        } else {
            // 5. Return safely if incorrect
            safe_return();
        }
    } else {
        // Return safely if user not found
        safe_return();
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
