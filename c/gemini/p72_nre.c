#include <stdio.h>
#include <string.h>
#include <ctype.h>

#define UPLOAD_DIR "uploads/"
#define MAX_PATH 256

int file_is_safe_type(const char *filename) {
    const char *ext = strrchr(filename, '.');
    if (!ext) return 0;
    if (strcasecmp(ext, ".jpg") == 0 || strcasecmp(ext, ".png") == 0) return 1;
    return 0;
}

void upload_file(const char *raw_filename) {
    if (!file_is_safe_type(raw_filename)) return;

    // 1. Sanitize: Find the last slash to strip path info
    const char *base_name = strrchr(raw_filename, '/');
    if (!base_name) base_name = strrchr(raw_filename, '\\');
    base_name = (base_name) ? base_name + 1 : raw_filename;

    // 2. Build safe path
    char safe_path[MAX_PATH];
    snprintf(safe_path, sizeof(safe_path), "%s%s", UPLOAD_DIR, base_name);

    // Proceed to open and write file...
    FILE *fptr = fopen(safe_path, "wb");
    if (fptr) {
        // Write logic here
        fclose(fptr);
    }
}