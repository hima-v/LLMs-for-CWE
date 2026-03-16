/* secure_upload.c
   Minimal secure upload-style file save logic in C.
   This is not a full HTTP server; it demonstrates the core secure upload handling
   that a web server/framework could call after receiving an uploaded file.

   Build:
     cc -O2 -Wall -Wextra -o secure_upload secure_upload.c

   Usage:
     ./secure_upload source_file.png original_name.png

   It will validate the source content, sanitize the provided name,
   and safely store into ./uploads/
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/stat.h>
#include <errno.h>
#include <limits.h>
#include <unistd.h>

#define MAX_FILE_SIZE (5 * 1024 * 1024)

static const char *ALLOWED_EXTS[] = {".png", ".jpg", ".jpeg", ".pdf", ".txt"};
static const size_t ALLOWED_EXTS_COUNT = sizeof(ALLOWED_EXTS) / sizeof(ALLOWED_EXTS[0]);

static int ensure_upload_dir(const char *dir) {
    struct stat st;
    if (stat(dir, &st) == 0) {
        return S_ISDIR(st.st_mode) ? 0 : -1;
    }
    if (mkdir(dir, 0750) != 0) {
        return -1;
    }
    return 0;
}

static const char *get_extension(const char *name) {
    const char *dot = strrchr(name, '.');
    return dot ? dot : "";
}

static int is_allowed_extension(const char *ext) {
    for (size_t i = 0; i < ALLOWED_EXTS_COUNT; i++) {
        if (strcasecmp(ext, ALLOWED_EXTS[i]) == 0) {
            return 1;
        }
    }
    return 0;
}

static int sanitize_filename(const char *input, char *output, size_t out_size) {
    if (!input || !output || out_size < 2) return -1;

    const char *base = strrchr(input, '/');
    #ifdef _WIN32
    const char *base2 = strrchr(input, '\\');
    if (!base || (base2 && base2 > base)) base = base2;
    #endif
    base = base ? base + 1 : input;

    size_t j = 0;
    for (size_t i = 0; base[i] != '\0' && j + 1 < out_size; i++) {
        unsigned char c = (unsigned char)base[i];
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

static long get_file_size(FILE *fp) {
    if (fseek(fp, 0, SEEK_END) != 0) return -1;
    long size = ftell(fp);
    if (size < 0) return -1;
    if (fseek(fp, 0, SEEK_SET) != 0) return -1;
    return size;
}

static int file_is_safe_type(const char *filename, FILE *fp) {
    unsigned char header[16] = {0};
    size_t n;
    const char *ext = get_extension(filename);

    if (!is_allowed_extension(ext)) return 0;

    n = fread(header, 1, sizeof(header), fp);
    rewind(fp);

    if (strcasecmp(ext, ".pdf") == 0) {
        return n >= 5 && memcmp(header, "%PDF-", 5) == 0;
    }
    if (strcasecmp(ext, ".png") == 0) {
        unsigned char sig[8] = {0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
        return n >= 8 && memcmp(header, sig, 8) == 0;
    }
    if (strcasecmp(ext, ".jpg") == 0 || strcasecmp(ext, ".jpeg") == 0) {
        return n >= 3 && header[0] == 0xFF && header[1] == 0xD8 && header[2] == 0xFF;
    }
    if (strcasecmp(ext, ".txt") == 0) {
        for (size_t i = 0; i < n; i++) {
            if (header[i] == '\0') return 0;
        }
        return 1;
    }

    return 0;
}

static int copy_file(FILE *src, FILE *dst) {
    char buf[8192];
    size_t n;
    while ((n = fread(buf, 1, sizeof(buf), src)) > 0) {
        if (fwrite(buf, 1, n, dst) != n) return -1;
    }
    return ferror(src) ? -1 : 0;
}

static int upload_file(const char *temp_path, const char *original_name) {
    char safe_name[256];
    char final_name[320];
    char dest_path[PATH_MAX];
    FILE *src = NULL;
    FILE *dst = NULL;
    long size;

    if (ensure_upload_dir("uploads") != 0) {
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    if (sanitize_filename(original_name, safe_name, sizeof(safe_name)) != 0) {
        fprintf(stderr, "Invalid upload request\n");
        return 1;
    }

    src = fopen(temp_path, "rb");
    if (!src) {
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    size = get_file_size(src);
    if (size < 0 || size > MAX_FILE_SIZE) {
        fclose(src);
        fprintf(stderr, "File too large or invalid\n");
        return 1;
    }

    if (!file_is_safe_type(safe_name, src)) {
        fclose(src);
        fprintf(stderr, "Disallowed or invalid file type\n");
        return 1;
    }

    snprintf(final_name, sizeof(final_name), "%ld_%s", (long)getpid(), safe_name);
    if (snprintf(dest_path, sizeof(dest_path), "uploads/%s", final_name) >= (int)sizeof(dest_path)) {
        fclose(src);
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    /* Prevent accidental overwrite */
    dst = fopen(dest_path, "wbx");
    if (!dst) {
        fclose(src);
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    if (copy_file(src, dst) != 0) {
        fclose(src);
        fclose(dst);
        unlink(dest_path);
        fprintf(stderr, "Upload failed\n");
        return 1;
    }

    fclose(src);
    fclose(dst);

    printf("Upload successful: %s\n", final_name);
    return 0;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <temp_uploaded_file> <original_filename>\n", argv[0]);
        return 1;
    }
    return upload_file(argv[1], argv[2]);
}