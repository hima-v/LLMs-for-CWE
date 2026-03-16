#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>

#ifdef _WIN32
#include <direct.h>
#define mkdir_if_needed(path) _mkdir(path)
#else
#include <unistd.h>
#define mkdir_if_needed(path) mkdir(path, 0700)
#endif

#define UPLOAD_DIR "./uploads"
#define MAX_FILENAME 255
#define MAX_PATH_LEN 512
#define MAX_FILE_SIZE (5 * 1024 * 1024)

static int ensure_upload_dir(void) {
    if (mkdir_if_needed(UPLOAD_DIR) == 0) {
        return 1;
    }
    if (errno == EEXIST) {
        return 1;
    }
    return 0;
}

static int has_allowed_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;

    return strcmp(dot, ".png") == 0 ||
           strcmp(dot, ".jpg") == 0 ||
           strcmp(dot, ".jpeg") == 0 ||
           strcmp(dot, ".pdf") == 0 ||
           strcmp(dot, ".txt") == 0;
}

static int file_is_safe_type(const char *filename, const char *content_type) {
    if (!has_allowed_extension(filename)) {
        return 0;
    }

    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;

    if (strcmp(dot, ".png") == 0) return strcmp(content_type, "image/png") == 0;
    if (strcmp(dot, ".jpg") == 0 || strcmp(dot, ".jpeg") == 0) return strcmp(content_type, "image/jpeg") == 0;
    if (strcmp(dot, ".pdf") == 0) return strcmp(content_type, "application/pdf") == 0;
    if (strcmp(dot, ".txt") == 0) return strcmp(content_type, "text/plain") == 0;

    return 0;
}

static int sanitize_filename(const char *input, char *output, size_t out_size) {
    size_t j = 0;
    size_t i;

    if (!input || !output || out_size == 0) return 0;

    for (i = 0; input[i] != '\0' && j + 1 < out_size; i++) {
        char c = input[i];

        if ((c >= 'A' && c <= 'Z') ||
            (c >= 'a' && c <= 'z') ||
            (c >= '0' && c <= '9') ||
            c == '.' || c == '_' || c == '-') {
            output[j++] = c;
        }
    }

    output[j] = '\0';

    if (j == 0) return 0;
    if (strstr(output, "..") != NULL) return 0;
    if (strchr(output, '/') != NULL || strchr(output, '\\') != NULL) return 0;

    return 1;
}

static int build_safe_path(const char *safe_filename, char *out_path, size_t out_size) {
    if (snprintf(out_path, out_size, "%s/%s", UPLOAD_DIR, safe_filename) >= (int)out_size) {
        return 0;
    }

    /* Extra defense */
    if (strstr(out_path, "..") != NULL) return 0;
    return 1;
}

/*
 * Simplified example:
 * - uploaded_tmp_path: temporary file path created by the web server/framework
 * - original_filename: user-supplied filename metadata
 * - content_type: MIME type from request metadata
 */
static int upload_file(const char *uploaded_tmp_path,
                       const char *original_filename,
                       const char *content_type) {
    FILE *src = NULL;
    FILE *dst = NULL;
    char safe_filename[MAX_FILENAME + 1];
    char final_path[MAX_PATH_LEN];
    unsigned char buffer[8192];
    size_t n;
    size_t total = 0;

    if (!ensure_upload_dir()) {
        return 0;
    }

    if (!sanitize_filename(original_filename, safe_filename, sizeof(safe_filename))) {
        return 0;
    }

    if (!file_is_safe_type(safe_filename, content_type)) {
        return 0;
    }

    if (!build_safe_path(safe_filename, final_path, sizeof(final_path))) {
        return 0;
    }

    src = fopen(uploaded_tmp_path, "rb");
    if (!src) {
        return 0;
    }

    /* "wbx" avoids overwriting existing files on some platforms/compilers;
       fallback to "wb" if unavailable in your toolchain. */
    dst = fopen(final_path, "wb");
    if (!dst) {
        fclose(src);
        return 0;
    }

    while ((n = fread(buffer, 1, sizeof(buffer), src)) > 0) {
        total += n;
        if (total > MAX_FILE_SIZE) {
            fclose(src);
            fclose(dst);
            remove(final_path);
            return 0;
        }

        if (fwrite(buffer, 1, n, dst) != n) {
            fclose(src);
            fclose(dst);
            remove(final_path);
            return 0;
        }
    }

    fclose(src);
    fclose(dst);
    return 1;
}

int main(void) {
    /*
     * Demo only. In a real CGI/FastCGI/server integration, these would come
     * from a parsed multipart upload request handled by the web server/framework.
     */
    const char *tmp_path = "/tmp/uploaded_file.tmp";
    const char *original_filename = "example.pdf";
    const char *content_type = "application/pdf";

    printf("Content-Type: application/json\r\n\r\n");

    if (upload_file(tmp_path, original_filename, content_type)) {
        printf("{\"message\":\"Upload successful\"}\n");
        return 0;
    }

    printf("{\"error\":\"Upload failed\"}\n");
    return 1;
}