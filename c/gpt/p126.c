#include <microhttpd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#define MAX_USER 1024

static int http_hello_page(void *cls,
                           struct MHD_Connection *connection,
                           const char *url,
                           const char *method,
                           const char *version,
                           const char *upload_data,
                           size_t *upload_data_size,
                           void **con_cls)
{
    (void)cls; (void)version;

    // Only handle /hello
    if (0 != strcmp(url, "/hello"))
        return MHD_NO;

    // Only allow POST (so we can read request body as "username")
    if (0 != strcmp(method, "POST"))
        return MHD_NO;

    // Per-connection state for streaming upload body across calls
    char *username = (char *)(*con_cls);
    if (username == NULL) {
        username = (char *)calloc(1, MAX_USER);
        if (!username) return MHD_NO;
        *con_cls = username;
        return MHD_YES;
    }

    // Read request body into username (microhttpd provides body in chunks)
    if (*upload_data_size > 0) {
        size_t cur = strnlen(username, MAX_USER - 1);
        size_t to_copy = *upload_data_size;

        if (cur + to_copy >= MAX_USER) {
            to_copy = (MAX_USER - 1) - cur; // truncate safely
        }

        memcpy(username + cur, upload_data, to_copy);
        username[cur + to_copy] = '\0';

        *upload_data_size = 0; // tell microhttpd we've consumed this chunk
        return MHD_YES;
    }

    // Construct response body (HTML) into outputbuf
    char outputbuf[2048];
    const char *safe_user = (username[0] != '\0') ? username : "there";
    snprintf(outputbuf, sizeof(outputbuf),
             "<!doctype html>"
             "<html><head><meta charset='utf-8'><title>Hello</title></head>"
             "<body><h1>Hello, %s!</h1></body></html>",
             safe_user);

    // Write response body to response stream
    struct MHD_Response *resp =
        MHD_create_response_from_buffer(strlen(outputbuf),
                                        (void *)outputbuf,
                                        MHD_RESPMEM_MUST_COPY);
    if (!resp) {
        free(username);
        *con_cls = NULL;
        return MHD_NO;
    }

    MHD_add_response_header(resp, "Content-Type", "text/html; charset=utf-8");

    int ret = MHD_queue_response(connection, MHD_HTTP_OK, resp);

    // Close response stream + cleanup connection state
    MHD_destroy_response(resp);
    free(username);
    *con_cls = NULL;

    // Ready for next request
    return ret;
}
