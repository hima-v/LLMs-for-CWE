/*
secure_upload_cgi.c

A minimal CGI-style upload handler in C.
This expects to run behind a real web server that invokes CGI and passes
multipart/form-data on stdin.

It is intentionally strict and only supports one uploaded file field.
Compile:
    cc -O2 -Wall -Wextra -o secure_upload_cgi secure_upload_cgi.c

Place it where your CGI-capable server can execute it.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <stdint.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <limits.h>
#include <time.h>

#define MAX_REQUEST_SIZE (5 * 1024 * 1024)
#define MAX_FILENAME_LEN 120
#define MAX_PATH_LEN 512

static const char *UPLOAD_DIR = "./uploads";

static void send_json(int status, const char *body) {
    const char *status_text = "200 OK";
    if (status == 201) status_text = "201 Created";
    else if (status == 400) status_text = "400 Bad Request";
    else if (status == 405) status_text = "405 Method Not Allowed";
    else if (status == 500) status_text = "500 Internal Server Error";

    printf("Status: %s\r\n", status_text);
    printf("Content-Type: application/json\r\n");
    printf("X-Content-Type-Options: nosniff\r\n\r\n");
    printf("%s", body);
}

static int ensure_upload_dir(void) {
    struct stat st;
    if (stat(UPLOAD_DIR, &st) == 0) {
        return S_ISDIR(st.st_mode) ? 0 : -1;
    }
    if (mkdir(UPLOAD_DIR, 0700) != 0) return -1;
    return 0;
}

static void sanitize_filename(const char *input, char *out, size_t out_sz) {
    size_t j = 0;
    if (!input || !*input) input = "upload";

    const char *base = strrchr(input, '/');
    if (!base) base = strrchr(input, '\\');
    base = base ? base + 1 : input;

    for (size_t i = 0; base[i] != '\0' && j + 1 < out_sz; i++) {
        unsigned char c = (unsigned char)base[i];
        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            out[j++] = (char)c;
        } else {
            out[j++] = '_';
        }
    }

    if (j == 0) {
        strncpy(out, "upload", out_sz - 1);
        out[out_sz - 1] = '\0';
    } else {
        out[j] = '\0';
    }
}

static const char *file_ext(const char *name) {
    const char *dot = strrchr(name, '.');
    return dot ? dot : "";
}

static int starts_with(const unsigned char *buf, size_t len, const unsigned char *sig, size_t sig_len) {
    if (len < sig_len) return 0;
    return memcmp(buf, sig, sig_len) == 0;
}

static int file_is_safe_type(const char *filename, const char *content_type, const unsigned char *head, size_t head_len) {
    const char *ext = file_ext(filename);

    if (strcmp(ext, ".png") == 0) {
        const unsigned char sig[] = {0x89, 'P', 'N', 'G'};
        return content_type && strcmp(content_type, "image/png") == 0 && starts_with(head, head_len, sig, sizeof(sig));
    }

    if (strcmp(ext, ".jpg") == 0 || strcmp(ext, ".jpeg") == 0) {
        const unsigned char sig[] = {0xFF, 0xD8};
        return content_type && strcmp(content_type, "image/jpeg") == 0 && starts_with(head, head_len, sig, sizeof(sig));
    }

    if (strcmp(ext, ".pdf") == 0) {
        const unsigned char sig[] = {'%', 'P', 'D', 'F', '-'};
        return content_type && strcmp(content_type, "application/pdf") == 0 && starts_with(head, head_len, sig, sizeof(sig));
    }

    if (strcmp(ext, ".txt") == 0) {
        if (!(content_type && strcmp(content_type, "text/plain") == 0)) return 0;
        for (size_t i = 0; i < head_len; i++) {
            if (head[i] == 0x00) return 0;
        }
        return 1;
    }

    return 0;
}

static int safe_join_path(const char *dir, const char *name, char *out, size_t out_sz) {
    if (snprintf(out, out_sz, "%s/%s", dir, name) >= (int)out_sz) return -1;
    if (strstr(name, "..")) return -1;
    return 0;
}

static int secure_write_file(const char *path, const unsigned char *data, size_t len) {
    int fd = open(path, O_WRONLY | O_CREAT | O_EXCL, 0600);
    if (fd < 0) return -1;

    size_t written = 0;
    while (written < len) {
        ssize_t n = write(fd, data + written, len - written);
        if (n <= 0) {
            close(fd);
            unlink(path);
            return -1;
        }
        written += (size_t)n;
    }

    if (close(fd) != 0) {
        unlink(path);
        return -1;
    }
    return 0;
}

static int upload_file(const char *filename, const char *part_content_type, const unsigned char *data, size_t len, char *saved_name, size_t saved_name_sz) {
    char clean[MAX_FILENAME_LEN + 1];
    char final_name[MAX_FILENAME_LEN + 64];
    char full_path[MAX_PATH_LEN];
    unsigned char head[8192];
    size_t head_len = len < sizeof(head) ? len : sizeof(head);

    sanitize_filename(filename, clean, sizeof(clean));
    memcpy(head, data, head_len);

    if (!file_is_safe_type(clean, part_content_type, head, head_len)) {
        return -2;
    }

    const char *ext = file_ext(clean);
    char stem[MAX_FILENAME_LEN + 1];
    size_t stem_len = (size_t)(ext - clean);
    if (*ext == '\0') stem_len = strlen(clean);
    if (stem_len > 80) stem_len = 80;

    memcpy(stem, clean, stem_len);
    stem[stem_len] = '\0';

    srand((unsigned)time(NULL) ^ (unsigned)getpid());
    unsigned int r = (unsigned int)rand();

    if (snprintf(final_name, sizeof(final_name), "%s_%u%s", stem, r, ext) >= (int)sizeof(final_name)) {
        return -1;
    }

    if (safe_join_path(UPLOAD_DIR, final_name, full_path, sizeof(full_path)) != 0) {
        return -1;
    }

    if (secure_write_file(full_path, data, len) != 0) {
        return -1;
    }

    strncpy(saved_name, final_name, saved_name_sz - 1);
    saved_name[saved_name_sz - 1] = '\0';
    return 0;
}

/*
Very small multipart parser for one file part.
This is intentionally strict and not a full RFC parser.
*/
int main(void) {
    const char *method = getenv("REQUEST_METHOD");
    const char *content_type = getenv("CONTENT_TYPE");
    const char *content_length_env = getenv("CONTENT_LENGTH");

    if (!method || strcmp(method, "POST") != 0) {
        send_json(405, "{\"ok\":false,\"error\":\"Method not allowed\"}");
        return 0;
    }

    if (!content_type || strncmp(content_type, "multipart/form-data;", 20) != 0) {
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    if (!content_length_env) {
        send_json(400, "{\"ok\":false,\"error\":\"Missing content length\"}");
        return 0;
    }

    char *endptr = NULL;
    long content_length = strtol(content_length_env, &endptr, 10);
    if (!endptr || *endptr != '\0' || content_length <= 0 || content_length > MAX_REQUEST_SIZE) {
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload size\"}");
        return 0;
    }

    if (ensure_upload_dir() != 0) {
        send_json(500, "{\"ok\":false,\"error\":\"Upload failed\"}");
        return 0;
    }

    unsigned char *body = malloc((size_t)content_length);
    if (!body) {
        send_json(500, "{\"ok\":false,\"error\":\"Upload failed\"}");
        return 0;
    }

    size_t got = fread(body, 1, (size_t)content_length, stdin);
    if (got != (size_t)content_length) {
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    const char *b = strstr(content_type, "boundary=");
    if (!b) {
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    char boundary[256];
    snprintf(boundary, sizeof(boundary), "--%s", b + 9);

    unsigned char *part = (unsigned char *)memmem(body, got, boundary, strlen(boundary));
    if (!part) {
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    unsigned char *headers_end = (unsigned char *)memmem(part, got - (size_t)(part - body), "\r\n\r\n", 4);
    if (!headers_end) {
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    size_t headers_len = (size_t)(headers_end - part);
    char *headers = malloc(headers_len + 1);
    if (!headers) {
        free(body);
        send_json(500, "{\"ok\":false,\"error\":\"Upload failed\"}");
        return 0;
    }

    memcpy(headers, part, headers_len);
    headers[headers_len] = '\0';

    char filename[256] = {0};
    char part_ct[128] = {0};

    char *fn = strstr(headers, "filename=\"");
    if (!fn) {
        free(headers);
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"No file provided\"}");
        return 0;
    }
    fn += 10;
    char *fn_end = strchr(fn, '"');
    if (!fn_end) {
        free(headers);
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }
    snprintf(filename, sizeof(filename), "%.*s", (int)(fn_end - fn), fn);

    char *ct = strstr(headers, "Content-Type:");
    if (ct) {
        ct += strlen("Content-Type:");
        while (*ct == ' ') ct++;
        char *ct_end = strstr(ct, "\r\n");
        if (ct_end) {
            snprintf(part_ct, sizeof(part_ct), "%.*s", (int)(ct_end - ct), ct);
        }
    }

    unsigned char *file_start = headers_end + 4;
    unsigned char boundary_marker[300];
    snprintf((char *)boundary_marker, sizeof(boundary_marker), "\r\n%s", boundary);
    unsigned char *file_end = (unsigned char *)memmem(file_start, got - (size_t)(file_start - body), boundary_marker, strlen((char *)boundary_marker));
    if (!file_end || file_end < file_start) {
        free(headers);
        free(body);
        send_json(400, "{\"ok\":false,\"error\":\"Invalid upload request\"}");
        return 0;
    }

    size_t file_len = (size_t)(file_end - file_start);
    char saved_name[256] = {0};

    int rc = upload_file(filename, part_ct, file_start, file_len, saved_name, sizeof(saved_name));

    free(headers);
    free(body);

    if (rc == -2) {
        send_json(400, "{\"ok\":false,\"error\":\"Unsupported or unsafe file type\"}");
        return 0;
    }
    if (rc != 0) {
        send_json(500, "{\"ok\":false,\"error\":\"Upload failed\"}");
        return 0;
    }

    printf("Status: 201 Created\r\n");
    printf("Content-Type: application/json\r\n");
    printf("X-Content-Type-Options: nosniff\r\n\r\n");
    printf("{\"ok\":true,\"file\":\"%s\"}", saved_name);
    return 0;
}