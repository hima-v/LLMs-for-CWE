#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sqlite3.h>

// Mock of a web framework redirect function
void redirect(const char *location) {
    printf("Redirecting to: %s\n", location);
}

void login_user(const char *username, const char *password) {
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int rc;

    // 1. Validate inputs
    if (username == NULL || password == NULL) {
        redirect("/login");
        return;
    }

    rc = sqlite3_open("users.db", &db);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "Cannot open database: %s\n", sqlite3_errmsg(db));
        sqlite3_close(db);
        // Error handling: redirect to login or error page
        redirect("/login"); 
        return;
    }

    // 2. Use parameterized queries (prepared statements)
    const char *sql = "SELECT id FROM users WHERE username = ? AND password = ?";
    
    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "Failed to prepare statement\n");
        sqlite3_close(db);
        redirect("/login");
        return;
    }

    // Bind parameters
    sqlite3_bind_text(stmt, 1, username, -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC); // In real app, hash password first

    // 3. Check credentials against database
    rc = sqlite3_step(stmt);
    
    if (rc == SQLITE_ROW) {
        // 4. Redirect only if authentication succeeds
        // (In a real app, set session cookie here)
        redirect("/dashboard");
    } else {
        // 5. Otherwise redirect to the login page
        redirect("/login");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
}

int main() {
    // Example usage
    login_user("admin", "secret");
    return 0;
}
