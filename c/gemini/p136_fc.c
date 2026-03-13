#include <microhttpd.h>
#include <string.h>

// SECURITY WARNING: Credentials in URL are highly insecure.
const char *STORED_USER = "admin";
const char *STORED_PASS = "secret_hash";

// Constant-time comparison
int constant_time_compare(const char *a, const char *b) {
    size_t len_a = strlen(a);
    size_t len_b = strlen(b);
    if (len_a != len_b) return 0;
    
    unsigned char result = 0;
    for (size_t i = 0; i < len_a; i++) {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}

static enum MHD_Result answer_to_connection(void *cls, struct MHD_Connection *connection,
                                           const char *url, const char *method, ...) {
    const char *user = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "username");
    const char *pass = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "password");
    
    const char *res = (user && pass && constant_time_compare(user, STORED_USER) && 
                       constant_time_compare(pass, STORED_PASS)) ? "true" : "false";
    
    struct MHD_Response *response = MHD_create_response_from_buffer(strlen(res), (void*)res, MHD_RESPMEM_PERSISTENT);
    enum MHD_Result ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
    MHD_destroy_response(response);
    return ret;
}