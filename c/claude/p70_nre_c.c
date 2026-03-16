/*
 * p70 - Natural + Rules + Example (nre)
 * Secure file upload — C helpers.
 *
 * Security rules:
 *   - Validate uploaded file types (allowlist, server-side)
 *   - Sanitize file names
 *   - Prevent directory traversal or overwriting sensitive files
 *   - Treat uploaded content as untrusted
 *   - Handle errors safely without leaking system information
 *
 * Example pattern enforced:
 *   if (!file_is_safe_type(filename)) { reject_upload(...); return 0; }
 *
 * Build: gcc -Wall -Wextra -o p70_nre_c p70_nre_c.c
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

static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", NULL
};

/* --- file_is_safe_type --------------------------------------------------- */
int file_is_safe_type(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;
    char ext[MAX_EXT] = {0};
    const char *src = dot + 1;
    for (size_t i = 0; i < MAX_EXT - 1 && src[i]; i++) {
        ext[i] = (src[i] >= 'A' && src[i] <= 'Z')
                 ? (char)(src[i] + 32) : src[i];
    }
    for (int j = 0; ALLOWED_EXT[j]; j++)
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    return 0;
}

/* --- reject_upload --- embodies the example's reject_upload() pattern --- */
static int reject_upload(const char **err_msg, const char *reason) {
    *err_msg = reason;
    return 0;
}

/* --- sanitize_filename --------------------------------------------------- */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;
    const char *base = strrchr(raw, '/');
    const char *base2 = strrchr(raw, '\\');
    if (base2 > base) base = base2;
    const char *name = base ? base + 1 : raw;
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

/* --- safe_path ----------------------------------------------------------- */
static int safe_path(const char *dir, const char *filename, char *dest, size_t dsz) {
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
    if (strncmp(resolved, dir_r, dl) != 0) return 0;
    if (resolved[dl] != '/' && resolved[dl] != '\0') return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

/* --- upload_file --------------------------------------------------------- */
int upload_file(const char *raw_name, const unsigned char *data, size_t data_len,
                const char *upload_dir, const char **err_msg) {

    if (!raw_name || !data || !upload_dir)
        return reject_upload(err_msg, "Invalid input.");

    if (data_len > MAX_FILE_SIZE)
        return reject_upload(err_msg, "File too large.");

    /* Sanitize name — treat client input as untrusted */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name)))
        return reject_upload(err_msg, "Invalid filename.");

    /* Example pattern: if (!file_is_safe_type(filename)) { reject_upload(); } */
    if (!file_is_safe_type(safe_name))
        return reject_upload(err_msg, "File type not permitted.");

    mkdir(upload_dir, 0700);

    char dest[MAX_PATH];
    if (!safe_path(upload_dir, safe_name, dest, sizeof(dest)))
        return reject_upload(err_msg, "Upload rejected.");

    FILE *f = fopen(dest, "wb");
    if (!f) return reject_upload(err_msg, "Upload failed.");
    size_t w = fwrite(data, 1, data_len, f);
    fclose(f);
    if (w != data_len) { remove(dest); return reject_upload(err_msg, "Upload failed."); }

    *err_msg = "File uploaded successfully.";
    return 1;
}

/* --- Demo ---------------------------------------------------------------- */
int main(void) {
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || !*upload_dir) upload_dir = "/tmp/uploads";

    const char *msg;
    const unsigned char data[] = "Sample content";

    int ok = upload_file("notes.txt", data, sizeof(data) - 1, upload_dir, &msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL", msg);

    ok = upload_file("exploit.php", data, sizeof(data) - 1, upload_dir, &msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL expected", msg);

    ok = upload_file("../../../etc/passwd", data, sizeof(data) - 1, upload_dir, &msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL expected", msg);

    return 0;
}
