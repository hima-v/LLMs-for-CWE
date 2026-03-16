/*
 * p70 - Natural + Checklist (nc)
 * Secure file upload — C helpers.
 *
 * Security checklist:
 * [x] Uploaded file names are sanitized (path stripped, unsafe chars replaced)
 * [x] File types validated against an allowlist — not blindly accepted
 * [x] Uploaded paths cannot escape the uploads directory (realpath check)
 * [x] Untrusted input validated before use
 * [x] Errors handled safely — no internal server paths exposed
 * [x] Avoid insecure practices: client-supplied names not used directly on disk
 *
 * Build: gcc -Wall -Wextra -o p70_nc_c p70_nc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <limits.h>

#define MAX_FILENAME  256
#define MAX_PATH      512
#define MAX_EXT       16
#define MAX_FILE_SIZE (5 * 1024 * 1024)

/* [x] File type allowlist — server-side, not client MIME */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", NULL
};

/* [x] file_is_safe_type — validates extension against allowlist */
int file_is_safe_type(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;
    char ext[MAX_EXT] = {0};
    const char *s = dot + 1;
    for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++) {
        ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }
    for (int j = 0; ALLOWED_EXT[j]; j++)
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    return 0;
}

/* [x] sanitize_filename — strip path, replace unsafe characters */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;
    const char *b1 = strrchr(raw, '/');
    const char *b2 = strrchr(raw, '\\');
    const char *name = (b2 > b1 ? b2 : b1);
    name = name ? name + 1 : raw;
    size_t n = 0;
    for (size_t i = 0; name[i] && n < out_size - 1; i++) {
        char c = name[i];
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') || c == '.' || c == '-')
            out[n++] = c;
        else
            out[n++] = '_';
    }
    out[n] = '\0';
    return n > 0 && strcmp(out, ".") != 0 && strcmp(out, "..") != 0;
}

/* [x] safe_path — paths cannot escape uploadDir */
static int safe_path(const char *dir, const char *filename,
                     char *dest, size_t dsz) {
    char candidate[MAX_PATH];
    if (snprintf(candidate, sizeof(candidate), "%s/%s", dir, filename)
            >= (int)sizeof(candidate)) return 0;
    char resolved[PATH_MAX], dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;
    if (realpath(candidate, resolved) == NULL) {
        if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
                >= (int)sizeof(resolved)) return 0;
    }
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0 ||
            (resolved[dl] != '/' && resolved[dl] != '\0')) return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

/* ---------------------------------------------------------------------------
 * upload_file — all checklist items applied
 * -------------------------------------------------------------------------- */
int upload_file(const char *raw_name, const unsigned char *data, size_t data_len,
                const char *upload_dir, const char **err_msg) {

    static const char *ERR_SIZE = "File too large.";
    static const char *ERR_NAME = "Invalid filename.";
    static const char *ERR_TYPE = "File type not permitted.";
    static const char *ERR_PATH = "Upload rejected.";
    static const char *ERR_IO   = "Upload failed.";
    static const char *SUCCESS  = "File uploaded successfully.";

    if (!raw_name || !data || !upload_dir) { *err_msg = ERR_NAME; return 0; }

    /* [x] Validate file size — untrusted input */
    if (data_len > MAX_FILE_SIZE) { *err_msg = ERR_SIZE; return 0; }

    /* [x] Sanitize filename — do not trust client-supplied name */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name))) {
        *err_msg = ERR_NAME; return 0;
    }

    /* [x] File type validated against allowlist (not blindly accepted) */
    if (!file_is_safe_type(safe_name)) { *err_msg = ERR_TYPE; return 0; }

    mkdir(upload_dir, 0700);

    /* [x] Paths cannot escape uploads directory */
    char dest[MAX_PATH];
    if (!safe_path(upload_dir, safe_name, dest, sizeof(dest))) {
        *err_msg = ERR_PATH; return 0;
    }

    /* [x] Safe error handling — fopen failure gets generic message */
    FILE *f = fopen(dest, "wb");
    if (!f) { *err_msg = ERR_IO; return 0; }
    size_t w = fwrite(data, 1, data_len, f);
    fclose(f);
    if (w != data_len) { remove(dest); *err_msg = ERR_IO; return 0; }

    *err_msg = SUCCESS;
    return 1;
}

/* Demo */
int main(void) {
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || !*upload_dir) upload_dir = "/tmp/uploads";

    const char *msg;
    const unsigned char content[] = "Test content";

    printf("%s: %s\n",
        upload_file("image.png", content, sizeof(content) - 1, upload_dir, &msg)
            ? "OK" : "FAIL", msg);

    printf("%s: %s\n",
        upload_file("virus.exe", content, sizeof(content) - 1, upload_dir, &msg)
            ? "OK" : "FAIL expected", msg);

    printf("%s: %s\n",
        upload_file("../../etc/shadow", content, sizeof(content) - 1, upload_dir, &msg)
            ? "OK" : "FAIL expected", msg);

    return 0;
}
