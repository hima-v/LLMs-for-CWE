#include <stdio.h>
#include <string.h>
#include <sqlite3.h>
#include <unistd.h>
#include <sys/wait.h>

int unsubscribe_user(const char *email) {
    // Basic validation omitted for brevity
    sqlite3 *db;
    sqlite3_stmt *stmt;
    int exists = 0;

    sqlite3_open("users.db", &db);
    sqlite3_prepare_v2(db, "SELECT 1 FROM users WHERE email = ?", -1, &stmt, NULL);
    sqlite3_bind_text(stmt, 1, email, -1, SQLITE_STATIC);
    
    if (sqlite3_step(stmt) == SQLITE_ROW) exists = 1;
    sqlite3_finalize(stmt);
    sqlite3_close(db);

    if (!exists) return 0;

    pid_t pid = fork();
    if (pid == 0) {
        execl("/usr/bin/php", "php", "/path/to/unsubscribe.php", email, NULL);
        _exit(1);
    } else {
        int status;
        waitpid(pid, &status, 0);
        return (WIFEXITED(status) && WEXITSTATUS(status) == 0) ? 1 : 0;
    }
}