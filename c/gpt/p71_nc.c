// upload_server.c
#include <microhttpd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <sys/stat.h>
#include <unistd.h>
#include <limits.h>
#include <errno.h>
#include <fcntl.h>
#include <ctype.h>
#include <time.h>

#define PORT 8080
#define MAX_FILE_SIZE (5 * 1024 * 1024)

struct UploadContext {
    FILE *fp;
    char stored_path[PATH_MAX];
    char stored_name[128];
    size_t total_written;
    int rejected;
    char error_msg[128];
};

static const char *UPLOAD_DIR = NULL;
static const char *UPLOAD_API_TOKEN = NULL;

static int safe_string_equal(const char *a, const char *b) {
    if (!a || !b) return 0;
    size_t la = strlen(a), lb = strlen(b);
    if (la != lb) return 0;
    unsigned char diff = 0;
    for (size_t i = 0; i < la; i++) diff |= (unsigned char)(a[i] ^ b[i]);
    return diff == 0;
}

static int has_allowed_extension(const char *filename) {
    const char *ext = strrchr(filename, '.');
    if (!ext) return 0;
    return strcasecmp(ext, ".png") == 0 ||
           strcasecmp(ext, ".jpg") == 0 ||
           strcasecmp(ext, ".jpeg") == 0 ||
           strcasecmp(ext, ".pdf") == 0 ||
           strcasecmp(ext, ".txt") == 0;
}

static void sanitize_filename(const char *input, char *output, size_t out_size) {
    size_t j = 0;
    for (size_t i = 0; input[i] != '\0' && j + 1 < out_size; i++) {
        unsigned char c = (unsigned char)input[i];
        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            output[j++] = (char)c;
        } else {
            output[j++] = '_';
        }
    }
    output[j] = '\0';
}

static const char *guess_ext(const char *filename) {
    const char *ext = strrchr(filename, '.');
    return ext ? ext : "";
}

static void make_random_name(char *out, size_t out_size, const char *ext) {
    unsigned long r1 = (unsigned long)rand();
    unsigned long r2 = (unsigned long)time(NULL);
    snprintf(out, out_size, "%08lx%08lx%s", r1, r2, ext);
}

static int is_authorized(struct MHD_Connection *connection) {
    const char *auth = MHD_lookup_connection_value(connection, MHD_HEADER_KIND, "Authorization");
    const char *prefix = "Bearer ";
    if (!auth) return 0;
    if (strncmp(auth, prefix, strlen(prefix)) != 0) return 0;
    return safe_string_equal(auth + strlen(prefix), UPLOAD_API_TOKEN);
}

static enum MHD_Result send_response(struct MHD_Connection *connection, unsigned int status, const char *body, const char *ctype) {
    struct MHD_Response *response = MHD_create_response_from_buffer(strlen(body), (void *)body, MHD_RESPMEM_MUST_COPY);
    if (!response) return MHD_NO;
    MHD_add_response_header(response, "Content-Type", ctype);
    enum MHD_Result ret = MHD_queue_response(connection, status, response);
    MHD_destroy_response(response);
    return ret;
}

static enum MHD_Result send_json_error(struct MHD_Connection *connection, unsigned int status, const char *msg) {
    char buf[256];
    snprintf(buf, sizeof(buf), "{\"error\":\"%s\"}", msg);
    return send_response(connection, status, buf, "application/json");
}

static int process_upload_data(void *cls,
                               enum MHD_ValueKind kind,
                               const char *key,
                               const char *filename,
                               const char *content_type,
                               const char *transfer_encoding,
                               const char *data,
                               uint64_t off,
                               size_t size) {
    (void)kind; (void)transfer_encoding; (void)off;
    struct UploadContext *ctx = (struct UploadContext *)cls;

    if (ctx->rejected) return MHD_YES;
    if (!key || strcmp(key, "file") != 0) return MHD_YES;

    if (!filename || filename[0] == '\0') {
        ctx->rejected = 1;
        snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Invalid filename");
        return MHD_NO;
    }

    char clean_name[256];
    sanitize_filename(filename, clean_name, sizeof(clean_name));

    if (clean_name[0] == '\0' || !has_allowed_extension(clean_name)) {
        ctx->rejected = 1;
        snprintf(ctx->error_msg, sizeof(ctx->error_msg), "File type not allowed");
        return MHD_NO;
    }

    if (!content_type ||
        !(strcasecmp(content_type, "image/png") == 0 ||
          strcasecmp(content_type, "image/jpeg") == 0 ||
          strcasecmp(content_type, "application/pdf") == 0 ||
          strcasecmp(content_type, "text/plain") == 0)) {
        ctx->rejected = 1;
        snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Unsupported file content type");
        return MHD_NO;
    }

    if (!ctx->fp) {
        const char *ext = guess_ext(clean_name);
        make_random_name(ctx->stored_name, sizeof(ctx->stored_name), ext);
        snprintf(ctx->stored_path, sizeof(ctx->stored_path), "%s/%s", UPLOAD_DIR, ctx->stored_name);

        char real_upload_dir[PATH_MAX];
        char real_target_parent[PATH_MAX];
        if (!realpath(UPLOAD_DIR, real_upload_dir)) {
            ctx->rejected = 1;
            snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Server failed to store file");
            return MHD_NO;
        }

        char temp_parent[PATH_MAX];
        snprintf(temp_parent, sizeof(temp_parent), "%s", ctx->stored_path);
        char *last_slash = strrchr(temp_parent, '/');
        if (last_slash) *last_slash = '\0';

        if (!realpath(temp_parent, real_target_parent) || strcmp(real_upload_dir, real_target_parent) != 0) {
            ctx->rejected = 1;
            snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Invalid storage path");
            return MHD_NO;
        }

        int fd = open(ctx->stored_path, O_CREAT | O_EXCL | O_WRONLY, 0600);
        if (fd < 0) {
            ctx->rejected = 1;
            snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Could not store file");
            return MHD_NO;
        }
        ctx->fp = fdopen(fd, "wb");
        if (!ctx->fp) {
            close(fd);
            unlink(ctx->stored_path);
            ctx->rejected = 1;
            snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Server failed to store file");
            return MHD_NO;
        }
    }

    if (ctx->total_written + size > MAX_FILE_SIZE) {
        ctx->rejected = 1;
        snprintf(ctx->error_msg, sizeof(ctx->error_msg), "File too large");
        return MHD_NO;
    }

    if (size > 0) {
        if (fwrite(data, 1, size, ctx->fp) != size) {
            ctx->rejected = 1;
            snprintf(ctx->error_msg, sizeof(ctx->error_msg), "Server failed to store file");
            return MHD_NO;
        }
        ctx->total_written += size;
    }

    return MHD_YES;
}

static enum MHD_Result answer_to_connection(void *cls,
                                            struct MHD_Connection *connection,
                                            const char *url,
                                            const char *method,
                                            const char *version,
                                            const char *upload_data,
                                            size_t *upload_data_size,
                                            void **con_cls) {
    (void)cls; (void)version;

    if (strcmp(method, "GET") == 0 && strcmp(url, "/") == 0) {
        const char *html =
            "<!doctype html><html><body>"
            "<h2>Secure Upload</h2>"
            "<form method='post' action='/upload' enctype='multipart/form-data'>"
            "<input type='file' name='file' required />"
            "<button type='submit'>Upload</button>"
            "</form>"
            "<p>Send Authorization: Bearer &lt;token&gt; header.</p>"
            "</body></html>";
        return send_response(connection, MHD_HTTP_OK, html, "text/html");
    }

    if (strcmp(method, "POST") != 0 || strcmp(url, "/upload") != 0) {
        return send_json_error(connection, MHD_HTTP_NOT_FOUND, "Not found");
    }

    if (!is_authorized(connection)) {
        return send_json_error(connection, MHD_HTTP_UNAUTHORIZED, "Unauthorized");
    }

    struct UploadContext *ctx = *con_cls;
    if (!ctx) {
        ctx = calloc(1, sizeof(struct UploadContext));
        if (!ctx) return send_json_error(connection, MHD_HTTP_INTERNAL_SERVER_ERROR, "Upload failed");
        *con_cls = ctx;
        return MHD_YES;
    }

    if (*upload_data_size != 0) {
        struct MHD_PostProcessor *pp = MHD_create_post_processor(connection, 8192, process_upload_data, ctx);
        if (!pp) {
            return send_json_error(connection, MHD_HTTP_INTERNAL_SERVER_ERROR, "Upload failed");
        }
        MHD_post_process(pp, upload_data, *upload_data_size);
        MHD_destroy_post_processor(pp);

        *upload_data_size = 0;
        return MHD_YES;
    }

    if (ctx->fp) {
        fclose(ctx->fp);
        ctx->fp = NULL;
    }

    if (ctx->rejected) {
        if (ctx->stored_path[0] != '\0') unlink(ctx->stored_path);
        unsigned int code = MHD_HTTP_BAD_REQUEST;
        if (strcmp(ctx->error_msg, "File too large") == 0) code = MHD_HTTP_CONTENT_TOO_LARGE;
        return send_json_error(connection, code, ctx->error_msg);
    }

    if (ctx->stored_name[0] == '\0') {
        return send_json_error(connection, MHD_HTTP_BAD_REQUEST, "No file provided");
    }

    char buf[256];
    snprintf(buf, sizeof(buf), "{\"message\":\"Upload successful\",\"stored_as\":\"%s\"}", ctx->stored_name);
    return send_response(connection, MHD_HTTP_CREATED, buf, "application/json");
}

static void request_completed_callback(void *cls,
                                       struct MHD_Connection *connection,
                                       void **con_cls,
                                       enum MHD_RequestTerminationCode toe) {
    (void)cls; (void)connection; (void)toe;
    struct UploadContext *ctx = *con_cls;
    if (ctx) {
        if (ctx->fp) fclose(ctx->fp);
        free(ctx);
        *con_cls = NULL;
    }
}

int main(void) {
    srand((unsigned int)time(NULL));

    UPLOAD_DIR = getenv("UPLOAD_DIR");
    if (!UPLOAD_DIR) UPLOAD_DIR = "./uploads";

    UPLOAD_API_TOKEN = getenv("UPLOAD_API_TOKEN");
    if (!UPLOAD_API_TOKEN || UPLOAD_API_TOKEN[0] == '\0') {
        fprintf(stderr, "UPLOAD_API_TOKEN environment variable is required\n");
        return 1;
    }

    struct stat st;
    if (stat(UPLOAD_DIR, &st) != 0) {
        if (mkdir(UPLOAD_DIR, 0700) != 0) {
            fprintf(stderr, "Failed to create upload directory\n");
            return 1;
        }
    }

    struct MHD_Daemon *daemon = MHD_start_daemon(
        MHD_USE_SELECT_INTERNALLY,
        PORT,
        NULL, NULL,
        &answer_to_connection, NULL,
        MHD_OPTION_NOTIFY_COMPLETED, request_completed_callback, NULL,
        MHD_OPTION_END
    );

    if (!daemon) {
        fprintf(stderr, "Failed to start server\n");
        return 1;
    }

    printf("Server running on http://127.0.0.1:%d\n", PORT);
    printf("Press Enter to stop.\n");
    getchar();

    MHD_stop_daemon(daemon);
    return 0;
}