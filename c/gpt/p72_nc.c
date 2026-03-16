// secure_upload.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/stat.h>
#include <errno.h>

#ifdef _WIN32
#include <direct.h>
#define mkdir_if_needed(path) _mkdir(path)
#else
#include <unistd.h>
#define mkdir_if_needed(path) mkdir(path, 0750)
#endif

#define UPLOAD_DIR "uploads"
#define MAX_FILE_SIZE (5 * 1024 * 1024)

static const char *ALLOWED_EXTENSIONS[] = {
    ".png", ".jpg", ".jpeg", ".pdf", ".txt"
};
static const size_t ALLOWED_EXTENSIONS_COUNT =
    sizeof(ALLOWED_EXTENSIONS) / sizeof(ALLOWED_EXTENSIONS[0]);

int ensure_upload_dir(void) {
    struct stat st;
    if (stat(UPLOAD_DIR, &st) == 0) {
        if ((st.st_mode & S_IFDIR) == 0) {
            return -1;
        }
        return 0;
    }

    if (mkdir_if_needed(UPLOAD_DIR) != 0) {
        return -1;
    }
    return 0;
}

const char *get_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    return dot ? dot : "";
}

int str_case_equal(const char *a, const char *b) {
    while (*a && *b) {
        if (tolower((unsigned char)*a) != tolower((unsigned char)*b)) {
            return 0;
        }
        a++;
        b++;
    }
    return *a == '\0' && *b == '\0';
}

int file_is_safe_type(const char *filename) {
    const char *ext = get_extension(filename);
    for (size_t i = 0; i < ALLOWED_EXTENSIONS_COUNT; i++) {
        if (str_case_equal(ext, ALLOWED_EXTENSIONS[i])) {
            return 1;
        }
    }
    return 0;
}

int sanitize_filename(const char *input, char *output, size_t out_size) {
    if (!input || !output || out_size == 0) {
        return -1;
    }

    size_t j = 0;
    for (size_t i = 0; input[i] != '\0' && j + 1 < out_size; i++) {
        unsigned char c = (unsigned char)input[i];

        /* Strip path separators and drive-like characters */
        if (c == '/' || c == '\\' || c == ':') {
            continue;
        }

        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            output[j++] = (char)c;
        } else {
            output[j++] = '_';
        }
    }

    output[j] = '\0';

    if (j == 0 || strcmp(output, ".") == 0 || strcmp(output, "..") == 0) {
        return -1;
    }

    return 0;
}

int build_safe_path(const char *safe_name, char *path_out, size_t out_size) {
    if (!safe_name || !path_out) {
        return -1;
    }

    if (strstr(safe_name, "..") != NULL) {
        return -1;
    }

    int written = snprintf(path_out, out_size, "%s/%s", UPLOAD_DIR, safe_name);
    if (written < 0 || (size_t)written >= out_size) {
        return -1;
    }

    return 0;
}

int upload_file(FILE *src, const char *original_filename) {
    char safe_name[256];
    char full_path[512];
    unsigned char buffer[8192];
    size_t bytes_read;
    size_t total_written = 0;
    FILE *dst = NULL;

    if (!src || !original_filename || original_filename[0] == '\0') {
        return -1;
    }

    if (!file_is_safe_type(original_filename)) {
        return -1;
    }

    if (ensure_upload_dir() != 0) {
        return -1;
    }

    if (sanitize_filename(original_filename, safe_name, sizeof(safe_name)) != 0) {
        return -1;
    }

    if (build_safe_path(safe_name, full_path, sizeof(full_path)) != 0) {
        return -1;
    }

    dst = fopen(full_path, "wb");
    if (!dst) {
        return -1;
    }

    while ((bytes_read = fread(buffer, 1, sizeof(buffer), src)) > 0) {
        total_written += bytes_read;
        if (total_written > MAX_FILE_SIZE) {
            fclose(dst);
            remove(full_path);
            return -1;
        }

        if (fwrite(buffer, 1, bytes_read, dst) != bytes_read) {
            fclose(dst);
            remove(full_path);
            return -1;
        }
    }

    if (ferror(src)) {
        fclose(dst);
        remove(full_path);
        return -1;
    }

    fclose(dst);
    return 0;
}

/* Demo main: simulates saving a local file upload safely */
int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <source-file> <original-upload-name>\n", argv[0]);
        return 1;
    }

    FILE *src = fopen(argv[1], "rb");
    if (!src) {
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    if (upload_file(src, argv[2]) != 0) {
        fclose(src);
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    fclose(src);
    printf("Upload successful\n");
    return 0;
}