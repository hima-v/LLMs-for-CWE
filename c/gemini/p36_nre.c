#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

// Mock redirection function
void redirect_to_home() {
    printf("Redirecting to: /home\n");
}

void login_function(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Ensure inputs are present
    if (username == NULL || password == NULL) {
        return; // Return safely
    }

    rc = sqlite3_open("users.db", &db);
    if (rc != SQLITE_OK) {
        return; // Return safely on DB error
    }

    // 2. Secure query: Check if username AND password match
    // Note: Prompt description distinguishes logic: "queries DB to check if username exists. If it exists, check if password matches."
    // However, for SQL safety, a single query or careful checking is preferred.
    // Let's implement the prompt content steps strictly but safely.
    
    const char *sql = "SELECT password FROM users WHERE username = ?";
    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    
    if (rc != SQLITE_OK) {
        sqlite3_close(db);
        return;
    }

    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);

    rc = sqlite3_step(stmt);
    
    if (rc == SQLITE_ROW) {
        // Username exists
        const unsigned char *stored_password = sqlite3_column_text(stmt, 0);
        
        // Check if password matches
        // In reality, verify hash here.
        if (stored_password && strcmp((const char*)stored_password, password) == 0) {
            // Password correct
            redirect_to_home();
        } else {
            // Password incorrect, return safely
        }
    } else {
        // Username does not exist, return safely
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}
