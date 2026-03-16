/*
Build example (Linux):
gcc secure_upload.c -o secure_upload -lmicrohttpd

Run:
./secure_upload

This is a minimal demo server using libmicrohttpd.
It accepts POST /upload with raw file body and header:
  X-Filename: example.pdf

For simplicity, it does not parse multipart/form-data.
*/

#include <microhttpd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <limits.h>
#include <errno.h>
#include <time.h>

#define PORT 8080
#define MAX_FILE_SIZE (10 * 1024 * 1024)
#define UPLOAD_DIR "./uploads"

struct UploadContext {
    FILE *fp;
    char filename[256];
    char path[PATH_MAX];
    size_t total_written;
    int failed;
};

static int file_is_safe_type(const char *filename) {
    /* Stub as requested. Replace in production. */
    (void)filename;
    return 1;
}

static const char *safe_json(const char *msg) {
    return msg ? msg : "Upload failed";
}

static int has_allowed_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;
    return strcasecmp(dot, ".txt") == 0 ||
           strcasecmp(dot, ".pdf") == 0 ||
           strcasecmp(dot, ".png") == 0 ||
           strcasecmp(dot, ".jpg") == 0 ||
           strcasecmp(dot, ".jpeg") == 0;
}

static void sanitize_filename(const char *input, char *output, size_t out_size) {
    size_t j = 0;
    const char *base = strrchr(input ? input : "", '/');
    base = base ? base + 1 : input;
    const char *base2 = strrchr(base ? base : "", '\\');
    base = base2 ? base2 + 1 : base;

    if (!base || !*base) base = "upload.bin";

    for (size_t i = 0; base[i] != '\0' && j + 1 < out_size; i++) {
        unsigned char c = (unsigned char)base[i];
        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            output[j++] = (char)c;
        } else {
            output[j++] = '_';
        }
    }
    output[j] = '\0';

    if (j == 0) {
        strncpy(output, "upload.bin", out_size - 1);
        output[out_size - 1] = '\0';
    }
}

static int ensure_upload_dir(void) {
    struct stat st;
    if (stat(UPLOAD_DIR, &st) == 0) {
        return S_ISDIR(st.st_mode) ? 0 : -1;
    }
    return mkdir(UPLOAD_DIR, 0700);
}

static int safe_destination_path(const char *filename, char *out, size_t out_size) {
    char real_upload[PATH_MAX];
    char combined[PATH_MAX];

    if (!realpath(UPLOAD_DIR, real_upload)) {
        return -1;
    }

    if (snprintf(combined, sizeof(combined), "%s/%s", real_upload, filename) >= (int)sizeof(combined)) {
        return -1;
    }

    /* Basic path escape prevention. Since filename is sanitized and basename-only, this is an extra check. */
    if (strstr(filename, "..") != NULL || strchr(filename, '/') != NULL || strchr(filename, '\\') != NULL) {
        return -1;
    }

    strncpy(out, combined, out_size - 1);
    out[out_size - 1] = '\0';
    return 0;
}

static int detect_file_type(const char *path, const char *filename) {
    unsigned char header[8] = {0};
    FILE *fp = fopen(path, "rb");
    const char *dot;

    if (!fp) return 0;
    size_t n = fread(header, 1, sizeof(header), fp);
    fclose(fp);

    dot = strrchr(filename, '.');
    if (!dot) return 0;

    if (strcasecmp(dot, ".pdf") == 0) {
        return n >= 5 &&
               header[0] == '%' &&
               header[1] == 'P' &&
               header[2] == 'D' &&
               header[3] == 'F' &&
               header[4] == '-';
    }

    if (strcasecmp(dot, ".png") == 0) {
        unsigned char png_sig[8] = {0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        return n == 8 && memcmp(header, png_sig, 8) == 0;
    }

    if (strcasecmp(dot, ".jpg") == 0 || strcasecmp(dot, ".jpeg") == 0) {
        return n >= 2 && header[0] == 0xFF && header[1] == 0xD8;
    }

    if (strcasecmp(dot, ".txt") == 0) {
        FILE *tf = fopen(path, "rb");
        if (!tf) return 0;
        unsigned char buf[1024];
        size_t r = fread(buf, 1, sizeof(buf), tf);
        fclose(tf);
        for (size_t i = 0; i < r; i++) {
            if (buf[i] == 0) return 0;
        }
        return 1;
    }

    return 0;
}

static enum MHD_Result send_json(struct MHD_Connection *connection, int status, const char *json) {
    struct MHD_Response *response = MHD_create_response_from_buffer(
        strlen(json), (void *)json, MHD_RESPMEM_MUST_COPY
    );
    if (!response) return MHD_NO;
    MHD_add_response_header(response, "Content-Type", "application/json");
    enum MHD_Result ret = MHD_queue_response(connection, status, response);
    MHD_destroy_response(response);
    return ret;
}

static enum MHD_Result upload_file(struct MHD_Connection *connection,
                                   const char *filename,
                                   const char *data,
                                   size_t *upload_data_size,
                                   void **con_cls) {
    struct UploadContext *ctx = *con_cls;

    if (!ctx) {
        ctx = calloc(1, sizeof(struct UploadContext));
        if (!ctx) {
            return send_json(connection, MHD_HTTP_INTERNAL_SERVER_ERROR,
                             "{\"error\":\"Upload failed\"}");
        }

        char clean[128];
        sanitize_filename(filename, clean, sizeof(clean));

        if (!file_is_safe_type(clean) || !has_allowed_extension(clean)) {
            free(ctx);
            return send_json(connection, MHD_HTTP_BAD_REQUEST,
                             "{\"error\":\"File type not allowed\"}");
        }

        char final_name[256];
        srand((unsigned int)time(NULL) ^ (unsigned int)getpid());
        snprintf(final_name, sizeof(final_name), "%u_%s", (unsigned int)rand(), clean);
        strncpy(ctx->filename, final_name, sizeof(ctx->filename) - 1);

        if (safe_destination_path(ctx->filename, ctx->path, sizeof(ctx->path)) != 0) {
            free(ctx);
            return send_json(connection, MHD_HTTP_BAD_REQUEST,
                             "{\"error\":\"Invalid upload path\"}");
        }

        ctx->fp = fopen(ctx->path, "wb");
        if (!ctx->fp) {
            free(ctx);
            return send_json(connection, MHD_HTTP_INTERNAL_SERVER_ERROR,
                             "{\"error\":\"Upload failed\"}");
        }

        *con_cls = ctx;
        return MHD_YES;
    }

    if (*upload_data_size != 0) {
        if (ctx->total_written + *upload_data_size > MAX_FILE_SIZE) {
            ctx->failed = 1;
            fclose(ctx->fp);
            remove(ctx->path);
            *upload_data_size = 0;
            return send_json(connection, MHD_HTTP_PAYLOAD_TOO_LARGE,
                             "{\"error\":\"File too large\"}");
        }

        if (fwrite(data, 1, *upload_data_size, ctx->fp) != *upload_data_size) {
            ctx->failed = 1;
            fclose(ctx->fp);
            remove(ctx->path);
            *upload_data_size = 0;
            return send_json(connection, MHD_HTTP_INTERNAL_SERVER_ERROR,
                             "{\"error\":\"Upload failed\"}");
        }

        ctx->total_written += *upload_data_size;
        *upload_data_size = 0;
        return MHD_YES;
    }

    fclose(ctx->fp);

    if (!ctx->failed && !detect_file_type(ctx->path, ctx->filename)) {
        remove(ctx->path);
        free(ctx);
        *con_cls = NULL;
        return send_json(connection, MHD_HTTP_BAD_REQUEST,
                         "{\"error\":\"Uploaded content does not match allowed file types\"}");
    }

    char response[512];
    snprintf(response, sizeof(response),
             "{\"message\":\"Upload successful\",\"stored_as\":\"%s\"}", ctx->filename);

    free(ctx);
    *con_cls = NULL;
    return send_json(connection, MHD_HTTP_CREATED, response);
}

static enum MHD_Result handler(void *cls,
                               struct MHD_Connection *connection,
                               const char *url,
                               const char *method,
                               const char *version,
                               const char *upload_data,
                               size_t *upload_data_size,
                               void **con_cls) {
    (void)cls;
    (void)version;

    if (strcmp(url, "/upload") != 0) {
        return send_json(connection, MHD_HTTP_NOT_FOUND, "{\"error\":\"Not found\"}");
    }

    if (strcmp(method, "POST") != 0) {
        return send_json(connection, MHD_HTTP_METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
    }

    const char *filename = MHD_lookup_connection_value(connection, MHD_HEADER_KIND, "X-Filename");
    if (!filename || strlen(filename) == 0) {
        return send_json(connection, MHD_HTTP_BAD_REQUEST, "{\"error\":\"Missing filename\"}");
    }

    return upload_file(connection, filename, upload_data, upload_data_size, con_cls);
}

int main(void) {
    if (ensure_upload_dir() != 0) {
        fprintf(stderr, "Failed to create uploads directory\n");
        return 1;
    }

    struct MHD_Daemon *daemon = MHD_start_daemon(
        MHD_USE_INTERNAL_POLLING_THREAD,
        PORT,
        NULL, NULL,
        &handler, NULL,
        MHD_OPTION_END
    );

    if (!daemon) {
        fprintf(stderr, "Failed to start server\n");
        return 1;
    }

    printf("Server running on http://127.0.0.1:%d\n", PORT);
    printf("POST raw file bytes to /upload with header X-Filename: yourfile.pdf\n");
    getchar();

    MHD_stop_daemon(daemon);
    return 0;
}