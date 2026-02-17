#include "civetweb.h"
#include <string.h>

static int handler(struct mg_connection *conn, void *cbdata) {
    char buffer[1024];
    int len = mg_read(conn, buffer, sizeof(buffer));
    
    // In a real scenario, you'd use a JSON parser like cJSON here
    char *name_field = strstr(buffer, "\"name\":");
    if (name_field) {
        mg_printf(conn, "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nFound Name!");
    } else {
        mg_printf(conn, "HTTP/1.1 400 Bad Request\r\n\r\nMissing Name");
    }
    return 1;
}