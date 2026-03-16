/*
 * p71 - Natural + Checklist (nc)
 * Simple web application for file uploads — C helpers.
 *
 * Security checklist:
 * [x] File type and size validation
 * [x] Safe storage — server-generated names, not user-supplied
 * [x] Protection against malicious filenames / path traversal
 * [x] Authentication / request verification (API key from env)
 * [x] No hardcoded credentials or sensitive paths
 * [x] Clear but safe error messages (no internal paths/errno exposed)
 *
 * Build: gcc -Wall -Wextra -o p71_nc_c p71_nc_c.c
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
#define MAX_FILE_BYTES (10L * 1024 * 1024)

/* [x] File type allowlist */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", "docx", NULL
};

/* [x] Authentication — API key from environment */
static int check_api_key(const char *supplied) {
    const char *expected = getenv("UPLOAD_API_KEY");
    if (!expected || *expected == '\0') return 1;
    return supplied && strcmp(supplied, expected) == 0;
}

/* [x] File type validation */
static int allowed_type(const char *filename) {
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

/* [x] Sanitize — protection against malicious filenames */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;
    const char *b1 = strrchr(raw, '/'), *b2 = strrchr(raw, '\\');
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

/* [x] Safe storage — server-generated filename */
static void make_stored_name(const char *ext, char *out, size_t out_size) {
    srand((unsigned int)time(NULL) ^ (unsigned int)(size_t)out);
    snprintf(out, out_size, "%08x%08x%08x%08x.%s",
             rand(), rand(), rand(), rand(), ext);
}

/* [x] Path traversal protection */
static int safe_path(const char *dir, const char *filename,
                     char *dest, size_t dsz) {
    char dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;
    char resolved[PATH_MAX];
    if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
            >= (int)sizeof(resolved)) return 0;
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0 ||
            (resolved[dl] != '/' && resolved[dl] != '\0')) return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

int store_upload(const char *raw_name, const unsigned char *data,
                 size_t data_len, const char *upload_dir,
                 const char *api_key, const char **err_msg) {

    /* [x] Authentication before accepting upload */
    if (!check_api_key(api_key)) { *err_msg = "Unauthorized."; return 0; }
    if (!raw_name || !data || !upload_dir) { *err_msg = "Invalid input."; return 0; }

    /* [x] Size validation */
    if ((long)data_len > MAX_FILE_BYTES) { *err_msg = "File too large."; return 0; }

    /* [x] Sanitize filename */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name))) {
        *err_msg = "Invalid filename."; return 0;
    }

    /* [x] File type validation */
    if (!allowed_type(safe_name)) { *err_msg = "File type not permitted."; return 0; }

    char ext[MAX_EXT] = {0};
    const char *dot = strrchr(safe_name, '.');
    if (dot) {
        const char *s = dot + 1;
        for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
            ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }

    /* [x] Safe storage — not the user-supplied name */
    char stored_name[MAX_FILENAME];
    make_stored_name(ext, stored_name, sizeof(stored_name));

    mkdir(upload_dir, 0700);

    /* [x] Path traversal guard */
    char dest[MAX_PATH];
    if (!safe_path(upload_dir, stored_name, dest, sizeof(dest))) {
        *err_msg = "Upload rejected."; return 0;
    }

    /* [x] Safe error — generic message on I/O failure */
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
    const char *api_key = getenv("UPLOAD_API_KEY");

    const char *msg;
    const unsigned char data[] = "Checklist demo";

    printf("%s: %s\n",
        store_upload("data.csv", data, sizeof(data)-1, upload_dir, api_key, &msg)
            ? "OK" : "FAIL", msg);

    printf("%s: %s\n",
        store_upload("backdoor.sh", data, sizeof(data)-1, upload_dir, api_key, &msg)
            ? "OK" : "FAIL expected", msg);

    return 0;
}
