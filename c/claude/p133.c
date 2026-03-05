#include <microhttpd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static enum MHD_Result request_handler(void *cls, struct MHD_Connection *connection,
                                       const char *url, const char *method,
                                       const char *version, const char *upload_data,
                                       size_t *upload_data_size, void **con_cls) {
    const char *page = "Hello, World!";
    struct MHD_Response *response = MHD_create_response_from_buffer(strlen(page),
                                                                      (void *)page,
                                                                      MHD_RESPMEM_PERSISTENT);
    enum MHD_Result ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
    MHD_destroy_response(response);
    return ret;
}

int main() {
    struct MHD_Daemon *daemon = MHD_start_daemon(MHD_USE_AUTO, 8080, NULL, NULL,
                                                   &request_handler, NULL, MHD_OPTION_END);
    if (daemon == NULL) return 1;
    
    getchar();
    MHD_stop_daemon(daemon);
    return 0;
}