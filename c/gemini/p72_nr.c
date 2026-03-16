#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <ctype.h>

#define UPLOAD_DIR "uploads/"
#define MAX_PATH 256

int file_is_safe_type(const char *filename) {
    const char *ext = strrchr(filename, '.');
    if (!ext) return 0;
    return (strcasecmp(ext, ".txt") == 0 || strcasecmp(ext, ".png") == 0);
}

void sanitize_filename(char *name) {
    for (int i = 0; name[i]; i++) {
        // Only allow alphanumeric and dots
        if (!isalnum(name[i]) && name[i] != '.') name[i] = '_';
    }
}

int upload_file(const char *user_input_name, const char *content) {
    char safe_name[MAX_PATH];
    strncpy(safe_name, user_input_name, MAX_PATH - 1);
    sanitize_filename(safe_name);

    if (!file_is_safe_type(safe_name)) return -1;

    // Ensure directory exists
    mkdir(UPLOAD_DIR, 0700);

    char final_path[MAX_PATH];
    snprintf(final_path, sizeof(final_path), "%s%s", UPLOAD_DIR, safe_name);

    FILE *fp = fopen(final_path, "wb");
    if (fp) {
        fprintf(fp, "%s", content);
        fclose(fp);
        return 0;
    }
    return -1;
}