#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/stat.h>
#include <errno.h>

#define MAX_FILENAME 200
#define MAX_PATH 512
#define MAX_UPLOAD_SIZE (10 * 1024 * 1024)

static const char *UPLOAD_DIR = "./uploads";

int file_is_safe_type(const char *filename) {
    /* Placeholder requested by user; still used in validation logic. */
    (void)filename;
    return 1;
}

int has_allowed_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;

    return strcmp(dot, ".txt") == 0 ||
           strcmp(dot, ".pdf") == 0 ||
           strcmp(dot, ".png") == 0 ||
           strcmp(dot, ".jpg") == 0 ||
           strcmp(dot, ".jpeg") == 0;
}

void sanitize_filename(const char *input, char *output, size_t out_size) {
    size_t j = 0;
    const char *base = strrchr(input, '/');
    #ifdef _WIN32
    const char *base2 = strrchr(input, '\\');
    if (!base || (base2 && base2 > base)) base = base2;
    #endif
    base = base ? base + 1 : input;

    for (size_t i = 0; base[i] != '\0' && j + 1 < out_size; i++) {
        unsigned char c = (unsigned char)base[i];
        if (isalnum(c) || c == '.' || c == '_' || c == '-') {
            output[j++] = (char)c;
        } else {
            output[j++] = '_';
        }
    }

    if (j == 0) {
        strncpy(output, "upload.bin", out_size - 1);
        output[out_size - 1] = '\0';
    } else {
        output[j] = '\0';
    }
}

int ensure_upload_dir(void) {
    struct stat st;
    if (stat(UPLOAD_DIR, &st) == 0) {
        if (!S_ISDIR(st.st_mode)) return 0;
        return 1;
    }
    if (mkdir(UPLOAD_DIR, 0700) != 0) {
        return 0;
    }
    return 1;
}

int build_safe_path(const char *filename, char *path, size_t path_size) {
    char safe_name[MAX_FILENAME + 1];
    sanitize_filename(filename, safe_name, sizeof(safe_name));

    if (strstr(safe_name, "..") != NULL) {
        return 0;
    }

    int written = snprintf(path, path_size, "%s/%s", UPLOAD_DIR, safe_name);
    if (written < 0 || (size_t)written >= path_size) {
        return 0;
    }

    return 1;
}

/*
 * Minimal example upload_file:
 * - reads raw bytes from stdin
 * - stores them using a provided filename
 * - assumes caller/web server already extracted the uploaded content
 *
 * For a real multipart parser, use a vetted library instead of hand-rolling it.
 */
int upload_file(const char *filename) {
    char path[MAX_PATH];
    unsigned char buffer[4096];
    size_t total = 0;

    if (filename == NULL || filename[0] == '\0') {
        fprintf(stderr, "Missing filename\n");
        return 0;
    }

    if (!has_allowed_extension(filename)) {
        fprintf(stderr, "Disallowed file type\n");
        return 0;
    }

    if (!file_is_safe_type(filename)) {
        fprintf(stderr, "Unsafe file type\n");
        return 0;
    }

    if (!ensure_upload_dir()) {
        fprintf(stderr, "Failed to prepare upload directory\n");
        return 0;
    }

    if (!build_safe_path(filename, path, sizeof(path))) {
        fprintf(stderr, "Invalid upload path\n");
        return 0;
    }

    FILE *out = fopen(path, "wb");
    if (!out) {
        fprintf(stderr, "Failed to open destination\n");
        return 0;
    }

    while (!feof(stdin)) {
        size_t n = fread(buffer, 1, sizeof(buffer), stdin);
        if (ferror(stdin)) {
            fclose(out);
            remove(path);
            fprintf(stderr, "Failed to read upload\n");
            return 0;
        }

        total += n;
        if (total > MAX_UPLOAD_SIZE) {
            fclose(out);
            remove(path);
            fprintf(stderr, "Upload too large\n");
            return 0;
        }

        if (n > 0 && fwrite(buffer, 1, n, out) != n) {
            fclose(out);
            remove(path);
            fprintf(stderr, "Failed to write file\n");
            return 0;
        }
    }

    if (fclose(out) != 0) {
        remove(path);
        fprintf(stderr, "Failed to finalize upload\n");
        return 0;
    }

    return 1;
}

int main(void) {
    /*
     * Demo CGI-like output.
     * In real use, filename would come from parsed multipart metadata,
     * not directly from untrusted environment variables without validation.
     */
    const char *filename = getenv("UPLOAD_FILENAME");

    printf("Content-Type: text/plain\r\n\r\n");

    if (!filename) {
        printf("Upload failed: missing filename metadata\n");
        return 1;
    }

    if (upload_file(filename)) {
        printf("Upload successful\n");
        return 0;
    }

    printf("Upload failed\n");
    return 1;
}