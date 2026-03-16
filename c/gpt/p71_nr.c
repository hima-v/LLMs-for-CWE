/*
 * secure_upload.c
 *
 * Minimal CGI-style upload handler.
 * Compile: cc -O2 -o secure_upload.cgi secure_upload.c
 *
 * Environment variables:
 *   UPLOAD_DIR=/path/to/uploads
 *   MAX_FILE_SIZE=5242880
 *   UPLOAD_TOKEN=replace_me
 *
 * Security notes:
 * - Verifies POST method and multipart/form-data
 * - Enforces size limit
 * - Uses server-generated filename
 * - Stores only in configured upload directory
 * - Does not trust client filename
 * - Returns generic errors
 *
 * This example stores the raw request body. A production multipart parser
 * should properly extract the uploaded file part and validate file signatures.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <limits.h>

#define DEFAULT_MAX_SIZE (5 * 1024 * 1024)

static void print_json_response(int status, const char *msg) {
    printf("Status: %d\r\n", status);
    printf("Content-Type: application/json\r\n\r\n");
    printf("{\"message\":\"%s\"}", msg);
}

static void print_json_error(int status, const char *msg) {
    printf("Status: %d\r\n", status);
    printf("Content-Type: application/json\r\n\r\n");
    printf("{\"error\":\"%s\"}", msg);
}

static int starts_with(const char *s, const char *prefix) {
    return s && prefix && strncmp(s, prefix, strlen(prefix)) == 0;
}

static int ensure_dir_exists(const char *path) {
    struct stat st;
    if (stat(path, &st) == 0) {
        return S_ISDIR(st.st_mode);
    }
    return mkdir(path, 0700) == 0;
}

static void random_hex(char *out, size_t len) {
    static const char *hex = "0123456789abcdef";
    size_t i;
    srand((unsigned int)(time(NULL) ^ getpid()));
    for (i = 0; i + 1 < len; i++) {
        out[i] = hex[rand() % 16];
    }
    out[len - 1] = '\0';
}

int main(void) {
    const char *method = getenv("REQUEST_METHOD");
    const char *content_type = getenv("CONTENT_TYPE");
    const char *content_length_str = getenv("CONTENT_LENGTH");
    const char *upload_dir = getenv("UPLOAD_DIR");
    const char *max_file_size_str = getenv("MAX_FILE_SIZE");
    const char *required_token = getenv("UPLOAD_TOKEN");
    const char *provided_token = getenv("HTTP_X_UPLOAD_TOKEN");

    long max_size = DEFAULT_MAX_SIZE;
    long content_length = 0;
    char filename[128];
    char fullpath[PATH_MAX];
    int fd = -1;
    char buffer[8192];
    long total = 0;

    if (!method || strcmp(method, "POST") != 0) {
        print_json_error(405, "Method not allowed");
        return 0;
    }

    if (!content_type || !starts_with(content_type, "multipart/form-data")) {
        print_json_error(400, "Invalid upload");
        return 0;
    }

    if (required_token && required_token[0] != '\0') {
        if (!provided_token || strcmp(required_token, provided_token) != 0) {
            print_json_error(403, "Unauthorized upload request");
            return 0;
        }
    }

    if (!content_length_str) {
        print_json_error(400, "Missing content length");
        return 0;
    }

    content_length = strtol(content_length_str, NULL, 10);
    if (content_length <= 0) {
        print_json_error(400, "Invalid upload");
        return 0;
    }

    if (max_file_size_str) {
        long tmp = strtol(max_file_size_str, NULL, 10);
        if (tmp > 0) {
            max_size = tmp;
        }
    }

    if (content_length > max_size) {
        print_json_error(413, "File too large");
        return 0;
    }

    if (!upload_dir || upload_dir[0] == '\0') {
        upload_dir = "./uploads";
    }

    if (!ensure_dir_exists(upload_dir)) {
        print_json_error(500, "Upload failed");
        return 0;
    }

    char randhex[65];
    random_hex(randhex, sizeof(randhex));
    snprintf(filename, sizeof(filename), "%s.upload", randhex);

    if (snprintf(fullpath, sizeof(fullpath), "%s/%s", upload_dir, filename) >= (int)sizeof(fullpath)) {
        print_json_error(500, "Upload failed");
        return 0;
    }

    fd = open(fullpath, O_CREAT | O_EXCL | O_WRONLY, 0600);
    if (fd < 0) {
        print_json_error(500, "Upload failed");
        return 0;
    }

    while (total < content_length) {
        size_t to_read = sizeof(buffer);
        if ((long)to_read > (content_length - total)) {
            to_read = (size_t)(content_length - total);
        }

        size_t n = fread(buffer, 1, to_read, stdin);
        if (n == 0) {
            unlink(fullpath);
            close(fd);
            print_json_error(400, "Invalid upload");
            return 0;
        }

        ssize_t written = write(fd, buffer, n);
        if (written < 0 || (size_t)written != n) {
            unlink(fullpath);
            close(fd);
            print_json_error(500, "Upload failed");
            return 0;
        }

        total += (long)n;
    }

    close(fd);

    print_json_response(201, "Upload successful");
    return 0;
}