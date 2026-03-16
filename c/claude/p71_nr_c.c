/*
 * p71 - Natural + Rules (nr)
 * Simple web application for file uploads — C helper layer.
 *
 * Rules enforced:
 *   - Validate uploaded file type and size
 *   - Avoid storing files with user-supplied names directly
 *   - Do not hardcode sensitive paths or credentials (UPLOAD_DIR from env)
 *   - Proper error handling for invalid uploads
 *   - Check upload request legitimacy (API key)
 *   - Prevent unsafe file execution (UUID names, no script extensions)
 *
 * Build: gcc -Wall -Wextra -o p71_nr_c p71_nr_c.c
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
#define MAX_FILE_BYTES (10L * 1024 * 1024)   /* 10 MB */

/* Allowed extensions — server-side allowlist; not from client MIME */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", "docx", NULL
};

/* ---------------------------------------------------------------------------
 * allowed_type — validate extension against allowlist
 * -------------------------------------------------------------------------- */
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

/* ---------------------------------------------------------------------------
 * sanitize_filename — strip path components, replace unsafe chars
 * -------------------------------------------------------------------------- */
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

/* ---------------------------------------------------------------------------
 * make_stored_name — generate a pseudo-UUID name (time+rand) to avoid
 * storing user-supplied filenames on disk
 * -------------------------------------------------------------------------- */
static void make_stored_name(const char *ext, char *out, size_t out_size) {
    srand((unsigned int)time(NULL) ^ (unsigned int)(size_t)out);
    snprintf(out, out_size, "%08x%08x%08x%08x.%s",
             rand(), rand(), rand(), rand(), ext);
}

/* ---------------------------------------------------------------------------
 * safe_path — resolve destination and confirm it stays inside upload_dir
 * -------------------------------------------------------------------------- */
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

/* ---------------------------------------------------------------------------
 * check_api_key — validate request legitimacy
 * -------------------------------------------------------------------------- */
static int check_api_key(const char *supplied) {
    const char *expected = getenv("UPLOAD_API_KEY");
    if (!expected || *expected == '\0') return 1;  /* key not set — open mode */
    if (!supplied) return 0;
    return strcmp(supplied, expected) == 0;
}

/* ---------------------------------------------------------------------------
 * store_upload — validate and save file data
 * -------------------------------------------------------------------------- */
int store_upload(const char *raw_name, const unsigned char *data,
                 size_t data_len, const char *upload_dir,
                 const char *api_key, const char **err_msg) {

    static const char *ERR_AUTH = "Unauthorized.";
    static const char *ERR_SIZE = "File too large.";
    static const char *ERR_NAME = "Invalid filename.";
    static const char *ERR_TYPE = "File type not permitted.";
    static const char *ERR_PATH = "Upload rejected.";
    static const char *ERR_IO   = "Upload failed.";
    static const char *SUCCESS  = "File uploaded successfully.";

    if (!check_api_key(api_key)) { *err_msg = ERR_AUTH; return 0; }
    if (!raw_name || !data || !upload_dir) { *err_msg = ERR_NAME; return 0; }
    if ((long)data_len > MAX_FILE_BYTES) { *err_msg = ERR_SIZE; return 0; }

    /* Sanitize — avoid storing user-supplied name directly */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name))) {
        *err_msg = ERR_NAME; return 0;
    }

    if (!allowed_type(safe_name)) { *err_msg = ERR_TYPE; return 0; }

    /* Build extension */
    char ext[MAX_EXT] = {0};
    const char *dot = strrchr(safe_name, '.');
    if (dot) {
        const char *s = dot + 1;
        for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
            ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }

    /* Generate server-controlled name — prevents execution via filename */
    char stored_name[MAX_FILENAME];
    make_stored_name(ext, stored_name, sizeof(stored_name));

    mkdir(upload_dir, 0700);

    char dest[MAX_PATH];
    if (!safe_path(upload_dir, stored_name, dest, sizeof(dest))) {
        *err_msg = ERR_PATH; return 0;
    }

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
    const char *api_key = getenv("UPLOAD_API_KEY");

    const char *msg;
    const unsigned char data[] = "Sample content";

    printf("%s: %s\n",
        store_upload("report.pdf", data, sizeof(data)-1, upload_dir, api_key, &msg)
            ? "OK" : "FAIL", msg);

    printf("%s: %s\n",
        store_upload("shell.php", data, sizeof(data)-1, upload_dir, api_key, &msg)
            ? "OK" : "FAIL expected", msg);

    printf("%s: %s\n",
        store_upload("../../etc/passwd", data, sizeof(data)-1, upload_dir, api_key, &msg)
            ? "OK" : "FAIL expected", msg);

    return 0;
}
