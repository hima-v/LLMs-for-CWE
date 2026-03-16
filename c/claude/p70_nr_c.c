/*
 * p70 - Natural + Rules (nr)
 * Secure file upload — C (CGI / libmicrohttpd-ready helpers).
 *
 * Rules enforced:
 *   - Validate file names and file types (allowlist)
 *   - Prevent directory traversal or arbitrary file writes
 *   - Avoid hardcoded paths or secrets (UPLOAD_DIR from environment)
 *   - Handle errors safely without exposing internal details
 *   - Treat all user input as untrusted
 *
 * The core helpers (file_is_safe_type, upload_file) can be integrated into
 * any C-based web framework or CGI handler. A minimal demo main() is included.
 *
 * Build: gcc -Wall -Wextra -o p70_nr_c p70_nr_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <limits.h>

#define MAX_FILENAME  256
#define MAX_PATH      512
#define MAX_EXT       16
#define MAX_FILE_SIZE (5 * 1024 * 1024)  /* 5 MB */

/* Allowed extensions — server-side allowlist */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", NULL
};

/* ---------------------------------------------------------------------------
 * file_is_safe_type
 * Returns 1 if the file extension (lower-cased, after last '.') is allowed.
 * Returns 0 otherwise. Does not trust any client-supplied MIME type.
 * -------------------------------------------------------------------------- */
int file_is_safe_type(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;

    /* Lower-case copy of extension */
    char ext[MAX_EXT] = {0};
    const char *src = dot + 1;
    size_t i;
    for (i = 0; i < MAX_EXT - 1 && src[i]; i++) {
        ext[i] = (char)((src[i] >= 'A' && src[i] <= 'Z')
                        ? src[i] + ('a' - 'A') : src[i]);
    }

    /* Check against allowlist */
    for (int j = 0; ALLOWED_EXT[j] != NULL; j++) {
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    }
    return 0;
}

/* ---------------------------------------------------------------------------
 * sanitize_filename
 * Strips any directory component, then replaces characters outside
 * [A-Za-z0-9._-] with '_'. Writes result into out (at most out_size bytes).
 * Returns 1 on success, 0 if the result is empty or a path-traversal attempt.
 * -------------------------------------------------------------------------- */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;

    /* Strip path components — take only basename */
    const char *base = strrchr(raw, '/');
    const char *base2 = strrchr(raw, '\\');
    if (base2 > base) base = base2;
    const char *name = base ? base + 1 : raw;

    size_t out_len = 0;
    for (size_t i = 0; name[i] && out_len < out_size - 1; i++) {
        char c = name[i];
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') || c == '.' || c == '-') {
            out[out_len++] = c;
        } else {
            out[out_len++] = '_';
        }
    }
    out[out_len] = '\0';
    return out_len > 0 && strcmp(out, ".") != 0 && strcmp(out, "..") != 0;
}

/* ---------------------------------------------------------------------------
 * safe_path
 * Builds an absolute destination path inside upload_dir.
 * Resolves symlinks/.. and returns 0 (failure) if the path escapes the dir.
 * -------------------------------------------------------------------------- */
static int safe_path(const char *upload_dir, const char *filename,
                     char *dest, size_t dest_size) {
    char candidate[MAX_PATH];
    if (snprintf(candidate, sizeof(candidate), "%s/%s", upload_dir, filename)
            >= (int)sizeof(candidate)) return 0;

    char resolved[PATH_MAX];
    /* Resolve symlinks; if file doesn't exist yet, resolve parent dir */
    if (realpath(candidate, resolved) == NULL) {
        /* File not yet created — resolve parent and reconstruct */
        char parent[MAX_PATH];
        snprintf(parent, sizeof(parent), "%s", upload_dir);
        char parent_resolved[PATH_MAX];
        if (realpath(parent, parent_resolved) == NULL) return 0;
        if (snprintf(resolved, sizeof(resolved), "%s/%s", parent_resolved, filename)
                >= (int)sizeof(resolved)) return 0;
    }

    /* Verify the resolved path starts with the resolved upload dir */
    char dir_resolved[PATH_MAX];
    if (realpath(upload_dir, dir_resolved) == NULL) return 0;
    size_t dir_len = strlen(dir_resolved);
    if (strncmp(resolved, dir_resolved, dir_len) != 0) return 0;
    if (resolved[dir_len] != '/' && resolved[dir_len] != '\0') return 0;

    if (snprintf(dest, dest_size, "%s", resolved) >= (int)dest_size) return 0;
    return 1;
}

/* ---------------------------------------------------------------------------
 * upload_file
 * Validates and stores data (from buffer) as a file in upload_dir.
 *   raw_name   : client-supplied filename (treated as untrusted)
 *   data       : file content
 *   data_len   : content length in bytes
 *   upload_dir : absolute path to the uploads directory
 * Returns 1 on success, 0 on failure. *err_msg is set to a safe string.
 * -------------------------------------------------------------------------- */
int upload_file(const char *raw_name, const unsigned char *data, size_t data_len,
                const char *upload_dir, const char **err_msg) {

    static const char *ERR_NAME  = "Invalid filename.";
    static const char *ERR_TYPE  = "File type not permitted.";
    static const char *ERR_SIZE  = "File too large.";
    static const char *ERR_PATH  = "Upload rejected.";
    static const char *ERR_IO    = "Upload failed.";
    static const char *SUCCESS   = "File uploaded successfully.";

    if (!raw_name || !data || !upload_dir) { *err_msg = ERR_NAME; return 0; }

    /* Validate file size */
    if (data_len > MAX_FILE_SIZE) { *err_msg = ERR_SIZE; return 0; }

    /* Sanitize filename — do not trust client input */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name))) {
        *err_msg = ERR_NAME; return 0;
    }

    /* Validate file type via allowlist */
    if (!file_is_safe_type(safe_name)) { *err_msg = ERR_TYPE; return 0; }

    /* Create upload directory if it doesn't exist */
    mkdir(upload_dir, 0700);

    /* Build and verify destination path — prevent directory traversal */
    char dest[MAX_PATH];
    if (!safe_path(upload_dir, safe_name, dest, sizeof(dest))) {
        *err_msg = ERR_PATH; return 0;
    }

    /* Write file — handle errors without leaking internal paths */
    FILE *f = fopen(dest, "wb");
    if (!f) { *err_msg = ERR_IO; return 0; }
    size_t written = fwrite(data, 1, data_len, f);
    fclose(f);
    if (written != data_len) { remove(dest); *err_msg = ERR_IO; return 0; }

    *err_msg = SUCCESS;
    return 1;
}

/* ---------------------------------------------------------------------------
 * Demo main()
 * -------------------------------------------------------------------------- */
int main(void) {
    /* UPLOAD_DIR from environment — no hardcoded path */
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || *upload_dir == '\0') upload_dir = "/tmp/uploads";

    const char *err_msg;
    const unsigned char sample_data[] = "Hello, world!";

    /* Valid upload */
    int ok = upload_file("report.txt", sample_data, sizeof(sample_data) - 1,
                         upload_dir, &err_msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL", err_msg);

    /* Invalid type */
    ok = upload_file("evil.sh", sample_data, sizeof(sample_data) - 1,
                     upload_dir, &err_msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL expected", err_msg);

    /* Directory traversal attempt */
    ok = upload_file("../../etc/passwd", sample_data, sizeof(sample_data) - 1,
                     upload_dir, &err_msg);
    printf("%s: %s\n", ok ? "OK" : "FAIL expected", err_msg);

    return 0;
}
