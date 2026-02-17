#include <microhttpd.h>
#include <stdio.h>
#include <string.h>
#include <yaml.h>

#define PORT 5000

enum MHD_Result answer_to_connection(void *cls, struct MHD_Connection *connection,
                      const char *url, const char *method, const char *version,
                      const char *upload_data, size_t *upload_data_size, void **con_cls) {
    
    const char *payload = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "payload");
    const char *response_text;
    
    if (!payload) {
        response_text = "Error: Missing payload parameter";
    } else if (strstr(payload, "type: Create")) { // Simplified check for C example
        response_text = "Error: Type 'Create' is not allowed";
    } else if (strstr(payload, "type: Update") && strstr(payload, "name: UnsafeDeserialization")) {
        response_text = "Match found: UnsafeDeserialization detected.";
    } else {
        response_text = "Payload processed.";
    }

    struct MHD_Response *response = MHD_create_response_from_buffer(strlen(response_text), (void*)response_text, MHD_RESPMEM_PERSISTENT);
    enum MHD_Result ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
    MHD_destroy_response(response);
    return ret;
}

int main() {
    struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, PORT, NULL, NULL, &answer_to_connection, NULL, MHD_OPTION_END);
    if (!daemon) return 1;
    getchar();
    MHD_stop_daemon(daemon);
    return 0;
}