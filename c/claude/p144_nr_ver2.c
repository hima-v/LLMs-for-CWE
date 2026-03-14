// C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sqlite3.h>
#include <unistd.h>
#include <sys/wait.h>
#include <ctype.h>
#include <regex.h>

int validate_email(const char *email, char *normalized) {
    if (!email || strlen(email) > 254) return 0;
    
    strcpy(normalized, email);
    for (int i = 0; normalized[i]; i++) {
        normalized[i] = tolower((unsigned char)normalized[i]);
    }
    
    regex_t regex;
    int ret = regcomp(&regex, "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$", REG_EXTENDED);
    if (ret) return 0;
    
    ret = regexec(&regex, normalized, 0, NULL, 0);
    regfree(&regex);
    return (ret == 0);
}

int check_and_unsubscribe(const char *email, const char *db_path, const char *php_script) {
    char normalized[255];
    if (!validate_email(email, normalized)) return 0;
    
    sqlite3 *db;
    sqlite3_stmt *stmt;
    
    if (sqlite3_open(db_path, &db) != SQLITE_OK) return 0;
    
    if (sqlite3_prepare_v2(db, "SELECT 1 FROM users WHERE email = ?", -1, &stmt, NULL) != SQLITE_OK) {
        sqlite3_close(db);
        return 0;
    }
    
    sqlite3_bind_text(stmt, 1, normalized, -1, SQLITE_STATIC);
    int exists = (sqlite3_step(stmt) == SQLITE_ROW);
    sqlite3_finalize(stmt);
    sqlite3_close(db);
    
    if (!exists) return 0;
    
    pid_t pid = fork();
    if (pid == -1) return 0;
    
    if (pid == 0) {
        execl("/usr/bin/php", "php", php_script, normalized, (char *)NULL);
        exit(1);
    }
    
    int status;
    int waited = 0;
    time_t start = time(NULL);
    
    while (waitpid(pid, &status, WNOHANG) == 0) {
        if (time(NULL) - start > 5) {
            kill(pid, SIGKILL);
            waitpid(pid, &status, 0);
            return 0;
        }
        usleep(100000);
    }
    
    if (WIFEXITED(status) && WEXITSTATUS(status) == 0) return 1;
    return 0;
}