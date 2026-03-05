#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>

// Web app structure
typedef struct {
    MYSQL *connection;
    int port;
} WebApp;

// Initialize database connection
MYSQL* connect_database(const char *host, const char *user, 
                       const char *password, const char *database) {
    MYSQL *conn = mysql_init(NULL);
    if (conn == NULL) {
        fprintf(stderr, "Database initialization failed\n");
        return NULL;
    }
    
    if (mysql_real_connect(conn, host, user, password, database, 0, NULL, 0) == NULL) {
        fprintf(stderr, "Database connection failed: %s\n", mysql_error(conn));
        return NULL;
    }
    
    return conn;
}

// Function to check if user is moderator
int check_moderator_status(MYSQL *conn, int user_id) {
    char query[256];
    MYSQL_RES *result;
    MYSQL_ROW row;
    int is_moderator = 0;
    
    snprintf(query, sizeof(query), 
             "SELECT is_moderator FROM users WHERE user_id = %d", user_id);
    
    if (mysql_query(conn, query)) {
        fprintf(stderr, "Query failed: %s\n", mysql_error(conn));
        return -1;
    }
    
    result = mysql_store_result(conn);
    if ((row = mysql_fetch_row(result)) != NULL) {
        is_moderator = atoi(row[0]);
    }
    
    mysql_free_result(result);
    return is_moderator;
}

// Route handler for moderator check
void handle_check_moderator(MYSQL *conn, int user_id) {
    int is_moderator = check_moderator_status(conn, user_id);
    
    if (is_moderator == -1) {
        printf("Error checking moderator status\n");
    } else {
        printf("{\"user_id\": %d, \"is_moderator\": %s}\n", 
               user_id, is_moderator ? "true" : "false");
    }
}

int main() {
    // Initialize web app
    WebApp app;
    app.port = 3000;
    
    // Connect to database
    app.connection = connect_database("localhost", "root", "password", "app_db");
    
    if (app.connection == NULL) {
        return 1;
    }
    
    // Example: Handle request for user_id = 5
    handle_check_moderator(app.connection, 5);
    
    // Close connection
    mysql_close(app.connection);
    
    return 0;
}