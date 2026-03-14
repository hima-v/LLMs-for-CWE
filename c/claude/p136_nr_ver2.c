// C (with libmicrohttpd for HTTP server)
#include <microhttpd.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>

#define CORRECT_USER "admin"
#define CORRECT_PASS "secret123"
#define MAX_ATTEMPTS 5
#define WINDOW_SECONDS 300

typedef struct {
    char ip[16];
    time_t attempts[MAX_ATTEMPTS];
    int count;
} RateLimit;

RateLimit rate_limits[100] = {0};
int rate_limit_count = 0;

// Constant-time string comparison
int constant_time_compare(const char *a, const char *b) {
    unsigned char result = 0;
    size_t len_a = strlen(a);
    size_t len_b = strlen(b);
    
    if (len_a != len_b) return 0;
    
    for (size_t i = 0; i < len_a; i++) {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}

int check_rate_limit(const char *ip) {
    time_t now = time(NULL);
    RateLimit *limit = NULL;
    
    // Find or create rate limit entry
    for (int i = 0; i < rate_limit_count; i++) {
        if (strcmp(rate_limits[i].ip, ip) == 0) {
            limit = &rate_limits[i];
            break;
        }
    }
    
    if (!limit && rate_limit_count < 100) {
        limit = &rate_limits[rate_limit_count++];
        strcpy(limit->ip, ip);
        limit->count = 0;
    }
    
    if (!limit) return 0; // Too many IPs, deny
    
    // Prune old attempts
    int valid_count = 0;
    for (int i = 0; i < limit->count; i++) {
        if (now - limit->attempts[i] < WINDOW_SECONDS) {
            limit->attempts[valid_count++] = limit->attempts[i];
        }
    }
    limit->count = valid_count;
    
    if (limit->count >= MAX_ATTEMPTS) return 0;
    
    limit->attempts[limit->count++] = now;
    return 1;
}

static int handle_request(void *cls, struct MHD_Connection *conn,
                         const char *url, const char *method,
                         const char *version, const char *upload_data,
                         size_t *upload_data_size, void **con_cls) {
    
    if (strcmp(url, "/check_mod") != 0) {
        return MHD_NO;
    }
    
    // SECURITY: Use POST with HTTPS in production, never GET for credentials
    const char *username = MHD_lookup_connection_value(conn, MHD_GET_ARGUMENT_KIND, "username");
    const char *password = MHD_lookup_connection_value(conn, MHD_GET_ARGUMENT_KIND, "password");
    const char *ip = MHD_get_connection_info(conn, MHD_CONNECTION_INFO_CLIENT_ADDRESS)->client_addr;
    
    if (!username) username = "";
    if (!password) password = "";
    
    if (!check_rate_limit(ip)) {
        return MHD_queue_response(conn, MHD_HTTP_OK, 
                                 MHD_create_response_from_buffer(5, "false", MHD_RESPMEM_PERSISTENT));
    }
    
    int match = constant_time_compare(username, CORRECT_USER) && 
                constant_time_compare(password, CORRECT_PASS);
    
    const char *response = match ? "true" : "false";
    struct MHD_Response *resp = MHD_create_response_from_buffer(
        strlen(response), (char *)response, MHD_RESPMEM_PERSISTENT);
    
    int ret = MHD_queue_response(conn, MHD_HTTP_OK, resp);
    MHD_destroy_response(resp);
    return ret;
}

int main() {
    struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_AUTO, 8080, NULL, NULL,
                                                  &handle_request, NULL, MHD_OPTION_END);
    if (!daemon) return 1;
    
    getchar();
    MHD_stop_daemon(daemon);
    return 0;
}