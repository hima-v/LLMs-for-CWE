/*
 * secure_upload.cgi.c
 *
 * Build example:
 *   cc -O2 -Wall -Wextra -o secure_upload.cgi secure_upload.cgi.c
 *
 * Environment variables:
 *   UPLOAD_DIR=/absolute/path/to/data_uploads
 *   UPLOAD_API_TOKEN=strong-random-token
 *   MAX_FILE_SIZE_BYTES=5242880
 *
 * Notes:
 * - Designed for multipart/form-data upload via CGI.
 * - Keeps logic simple for demonstration.
 * - Stores uploaded files with random ".upload" names, never user-provided names.
 * - Assumes the CGI server passes request body on stdin and CONTENT_LENGTH/CONTENT_TYPE in env.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <limits.h>
#include <errno.h>
#include <time.h>

#define DEFAULT_MAX_FILE_SIZE (5 * 1024 * 1024)
#define BUF_SIZE 8192

static void send_json(int status, const char *json) {
    const char *status_text = "200 OK";
    if (status == 201) status_text = "201 Created";
    else if (status == 400) status_text = "400 Bad Request";
    else if (status == 401) status_text = "401 Unauthorized";
    else if (status == 404) status_text = "404 Not Found";
    else if (status == 409) status_text = "409 Conflict";
    else if (status == 413) status_text = "413 Payload Too Large";
    else if (status == 500) status_text = "500 Internal Server Error";

    printf("Status: %s\r\n", status_text);
    printf("Content-Type: application/json\r\n");
    printf("Cache-Control: no-store\r\n\r\n");
    printf("%s", json);
}

static int constant_time_equals(const char *a, const char *b) {
    size_t la = a ? strlen(a) : 0;
    size_t lb = b ? strlen(b) : 0;
    size_t n = la > lb ? la : lb;
    unsigned char diff = (unsigned char)(la ^ lb);
    for (size_t i = 0; i < n; i++) {
        unsigned char ca = i < la ? (unsigned char)a[i] : 0;
        unsigned char cb = i < lb ? (unsigned char)b[i] : 0;
        diff |= (unsigned char)(ca ^ cb);
    }
    return diff == 0;
}

static int is_allowed_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;
    if (strcasecmp(dot, ".png") == 0) return 1;
    if (strcasecmp(dot, ".jpg") == 0) return 1;
    if (strcasecmp(dot, ".jpeg") == 0) return 1;
    if (strcasecmp(dot, ".pdf") == 0) return 1;
    if (strcasecmp(dot, ".txt") == 0) return 1;
    return 0;
}

static int sanitize_filename(const char *in, char *out, size_t out_sz) {
    if (!in || !*in || out_sz < 2) return 0;

    // basename only
    const char *base = strrchr(in, '/');
    if (base) in = base + 1;
    base = strrchr(in, '\\');
    if (base) in = base + 1;

    size_t j = 0;
    for (size_t i = 0; in[i] != '\0' && j + 1 < out_sz; i++) {
        unsigned char c = (unsigned char)in[i];
        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            out[j++] = (char)c;
        } else {
            out[j++] = '_';
        }
    }
    out[j] = '\0';

    if (j == 0) return 0;
    if (strcmp(out, ".") == 0 || strcmp(out, "..") == 0) return 0;
    return 1;
}

static int make_random_name(char *out, size_t out_sz) {
    const char *hex = "0123456789abcdef";
    unsigned char buf[16];
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd < 0) return 0;
    ssize_t n = read(fd, buf, sizeof(buf));
    close(fd);
    if (n != (ssize_t)sizeof(buf)) return 0;

    if (out_sz < 16 * 2 + 8) return 0;
    for (int i = 0; i < 16; i++) {
        out[i * 2] = hex[(buf[i] >> 4) & 0xF];
        out[i * 2 + 1] = hex[buf[i] & 0xF];
    }
    strcpy(out + 32, ".upload");
    return 1;
}

static int safe_join_path(const char *base, const char *name, char *out, size_t out_sz) {
    if (!base || !name || strchr(name, '/')) return 0;
    int written = snprintf(out, out_sz, "%s/%s", base, name);
    if (written < 0 || (size_t)written >= out_sz) return 0;
    return 1;
}

static const char *get_header_value_from_env(const char *header_env_name) {
    return getenv(header_env_name);
}

static void show_form(void) {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!doctype html><html><body>");
    printf("<h2>Secure File Upload</h2>");
    printf("<form action=\"\" method=\"post\" enctype=\"multipart/form-data\">");
    printf("<input type=\"file\" name=\"file\" required />");
    printf("<button type=\"submit\">Upload</button>");
    printf("</form>");
    printf("<p>Send header: X-Upload-Token</p>");
    printf("</body></html>");
}

/*
 * Very small multipart parser for one file field named "file".
 * For clarity, this reads the full request into memory, which is acceptable here only because
 * we enforce a small max size. For larger systems, stream parse instead.
 */
int main(void) {
    const char *method = getenv("REQUEST_METHOD");
    if (!method || strcmp(method, "GET") == 0) {
        show_form();
        return 0;
    }

    if (strcmp(method, "POST") != 0) {
        send_json(400, "{\"error\":\"Invalid request.\"}");
        return 0;
    }

    const char *api_token = getenv("UPLOAD_API_TOKEN");
    const char *provided_token = get_header_value_from_env("HTTP_X_UPLOAD_TOKEN");
    if (!api_token || !*api_token) {
        send_json(500, "{\"error\":\"Server not configured securely.\"}");
        return 0;
    }
    if (!constant_time_equals(provided_token ? provided_token : "", api_token)) {
        send_json(401, "{\"error\":\"Unauthorized.\"}");
        return 0;
    }

    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || upload_dir[0] != '/') {
        send_json(500, "{\"error\":\"Server not configured securely.\"}");
        return 0;
    }

    const char *max_str = getenv("MAX_FILE_SIZE_BYTES");
    long max_size = max_str ? strtol(max_str, NULL, 10) : DEFAULT_MAX_FILE_SIZE;
    if (max_size <= 0) max_size = DEFAULT_MAX_FILE_SIZE;

    const char *cl = getenv("CONTENT_LENGTH");
    if (!cl) {
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }
    long content_length = strtol(cl, NULL, 10);
    if (content_length <= 0 || content_length > max_size + 8192) {
        send_json(413, "{\"error\":\"File too large.\"}");
        return 0;
    }

    const char *ct = getenv("CONTENT_TYPE");
    if (!ct || strncmp(ct, "multipart/form-data;", 20) != 0) {
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }

    const char *bpos = strstr(ct, "boundary=");
    if (!bpos) {
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }
    const char *boundary = bpos + 9;
    char boundary_marker[512];
    if (snprintf(boundary_marker, sizeof(boundary_marker), "--%s", boundary) >= (int)sizeof(boundary_marker)) {
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }

    char *body = malloc((size_t)content_length + 1);
    if (!body) {
        send_json(500, "{\"error\":\"Upload failed.\"}");
        return 0;
    }

    size_t total = 0;
    while ((long)total < content_length) {
        ssize_t n = fread(body + total, 1, (size_t)(content_length - (long)total), stdin);
        if (n <= 0) {
            free(body);
            send_json(400, "{\"error\":\"Invalid upload request.\"}");
            return 0;
        }
        total += (size_t)n;
    }
    body[total] = '\0';

    char *file_part = strstr(body, "Content-Disposition:");
    if (!file_part) {
        free(body);
        send_json(400, "{\"error\":\"No file provided.\"}");
        return 0;
    }

    char *name_field = strstr(file_part, "name=\"file\"");
    char *filename_field = strstr(file_part, "filename=\"");
    if (!name_field || !filename_field) {
        free(body);
        send_json(400, "{\"error\":\"No file provided.\"}");
        return 0;
    }

    filename_field += 10;
    char *filename_end = strchr(filename_field, '"');
    if (!filename_end) {
        free(body);
        send_json(400, "{\"error\":\"Invalid filename.\"}");
        return 0;
    }

    char original_name[256];
    size_t fname_len = (size_t)(filename_end - filename_field);
    if (fname_len == 0 || fname_len >= sizeof(original_name)) {
        free(body);
        send_json(400, "{\"error\":\"Invalid filename.\"}");
        return 0;
    }
    memcpy(original_name, filename_field, fname_len);
    original_name[fname_len] = '\0';

    char safe_name[256];
    if (!sanitize_filename(original_name, safe_name, sizeof(safe_name))) {
        free(body);
        send_json(400, "{\"error\":\"Invalid filename.\"}");
        return 0;
    }

    if (!is_allowed_extension(safe_name)) {
        free(body);
        send_json(400, "{\"error\":\"Unsupported file type.\"}");
        return 0;
    }

    char *headers_end = strstr(filename_end, "\r\n\r\n");
    if (!headers_end) {
        free(body);
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }
    unsigned char *file_data = (unsigned char *)(headers_end + 4);

    char boundary_search[520];
    snprintf(boundary_search, sizeof(boundary_search), "\r\n%s", boundary_marker);
    char *file_end = strstr((char *)file_data, boundary_search);
    if (!file_end) {
        free(body);
        send_json(400, "{\"error\":\"Invalid upload request.\"}");
        return 0;
    }

    size_t file_size = (size_t)(file_end - (char *)file_data);
    if (file_size == 0 || (long)file_size > max_size) {
        free(body);
        send_json(400, "{\"error\":\"Invalid file size.\"}");
        return 0;
    }

    char stored_name[64];
    if (!make_random_name(stored_name, sizeof(stored_name))) {
        free(body);
        send_json(500, "{\"error\":\"Upload failed.\"}");
        return 0;
    }

    char full_path[PATH_MAX];
    if (!safe_join_path(upload_dir, stored_name, full_path, sizeof(full_path))) {
        free(body);
        send_json(500, "{\"error\":\"Upload failed.\"}");
        return 0;
    }

    mkdir(upload_dir, 0700);

    int fd = open(full_path, O_WRONLY | O_CREAT | O_EXCL, 0600);
    if (fd < 0) {
        free(body);
        if (errno == EEXIST) {
            send_json(409, "{\"error\":\"Upload collision, retry.\"}");
        } else {
            send_json(500, "{\"error\":\"Upload failed.\"}");
        }
        return 0;
    }

    size_t written = 0;
    while (written < file_size) {
        ssize_t n = write(fd, file_data + written, file_size - written);
        if (n <= 0) {
            close(fd);
            unlink(full_path);
            free(body);
            send_json(500, "{\"error\":\"Upload failed.\"}");
            return 0;
        }
        written += (size_t)n;
    }

    close(fd);
    free(body);

    char response[256];
    snprintf(response, sizeof(response),
             "{\"message\":\"Upload successful.\",\"stored_as\":\"%s\"}", stored_name);
    send_json(201, response);
    return 0;
}