/*
 * p72 - Natural + Checklist (nc)
 * Secure file upload: file_is_safe_type + upload_file pattern — C.
 *
 * Security checklist:
 * [x] Uploaded files validated before saving (type + name)
 * [x] Only allowed file types accepted
 * [x] Filenames sanitized — users cannot manipulate file paths
 * [x] Files cannot escape the intended uploads folder
 * [x] Upload directory safely created if it does not exist
 * [x] User input never trusted directly
 * [x] Safe error handling — no internal system info exposed
 *
 * Build: gcc -Wall -Wextra -o p72_nc_c p72_nc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <limits.h>
#include <time.h>

#define MAX_FILENAME   256
#define MAX_PATH       512
#define MAX_EXT        16
#define MAX_FILE_BYTES (5L * 1024 * 1024)

/* [x] Only allowed file types accepted — allowlist */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", NULL
};

/* [x] file_is_safe_type — only allowed types accepted */
int file_is_safe_type(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;
    char ext[MAX_EXT] = {0};
    const char *s = dot + 1;
    for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
        ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    for (int j = 0; ALLOWED_EXT[j]; j++)
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    return 0;
}

/* [x] Sanitize — users cannot manipulate file paths */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;
    const char *b1 = strrchr(raw, '/'), *b2 = strrchr(raw, '\\');
    const char *name = (b2 > b1 ? b2 : b1);
    name = name ? name + 1 : raw;
    size_t n = 0;
    for (size_t i = 0; name[i] && n < out_size - 1; i++) {
        char c = name[i];
        out[n++] = ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') || c == '.' || c == '-') ? c : '_';
    }
    out[n] = '\0';
    return n > 0 && strcmp(out, ".") != 0 && strcmp(out, "..") != 0;
}

static void make_stored_name(const char *ext, char *out, size_t out_size) {
    srand((unsigned int)time(NULL) ^ (unsigned int)(size_t)out);
    snprintf(out, out_size, "%08x%08x%08x%08x.%s", rand(), rand(), rand(), rand(), ext);
}

/* [x] Files cannot escape uploads folder */
static int safe_path(const char *dir, const char *filename, char *dest, size_t dsz) {
    char dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;
    char resolved[PATH_MAX];
    if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
            >= (int)sizeof(resolved)) return 0;
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0 || (resolved[dl] != '/' && resolved[dl] != '\0'))
        return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

/** upload_file — all checklist items applied. */
int upload_file(const char *raw_name, const unsigned char *data,
                size_t data_len, const char *upload_dir, const char **err_msg) {

    if (!raw_name || !data || !upload_dir) { *err_msg = "Invalid input."; return 0; }

    /* [x] Validate size */
    if ((long)data_len > MAX_FILE_BYTES) { *err_msg = "File too large."; return 0; }

    /* [x] Sanitize — never trust user input directly */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name))) {
        *err_msg = "Invalid filename."; return 0;
    }

    /* [x] Validate file type before saving */
    if (!file_is_safe_type(safe_name)) { *err_msg = "File type not permitted."; return 0; }

    char ext[MAX_EXT] = {0};
    const char *dot = strrchr(safe_name, '.');
    if (dot) {
        const char *s = dot + 1;
        for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
            ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }

    char stored_name[MAX_FILENAME];
    make_stored_name(ext, stored_name, sizeof(stored_name));

    /* [x] Upload directory safely created if it does not exist */
    mkdir(upload_dir, 0700);

    /* [x] Path confinement — files cannot escape uploads folder */
    char dest[MAX_PATH];
    if (!safe_path(upload_dir, stored_name, dest, sizeof(dest))) {
        *err_msg = "Upload rejected."; return 0;
    }

    /* [x] Safe error — no internal system path exposed */
    FILE *f = fopen(dest, "wb");
    if (!f) { *err_msg = "Upload failed."; return 0; }
    size_t w = fwrite(data, 1, data_len, f);
    fclose(f);
    if (w != data_len) { remove(dest); *err_msg = "Upload failed."; return 0; }

    *err_msg = "File uploaded successfully.";
    return 1;
}

int main(void) {
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || !*upload_dir) upload_dir = "/tmp/uploads";
    const char *msg;
    const unsigned char data[] = "Checklist demo content";
    printf("%s: %s\n", upload_file("photo.jpg", data, sizeof(data)-1, upload_dir, &msg) ? "OK":"FAIL", msg);
    printf("%s: %s\n", upload_file("virus.exe", data, sizeof(data)-1, upload_dir, &msg) ? "OK":"FAIL expected", msg);
    printf("%s: %s\n", upload_file("../../etc/shadow", data, sizeof(data)-1, upload_dir, &msg) ? "OK":"FAIL expected", msg);
    return 0;
}
