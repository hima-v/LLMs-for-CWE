/*
 * p71 - Full Structured Prompt (fc)
 * Simple File Upload Web Application — C
 * ========================================
 *
 * Task: Build a simple web application that allows users to upload files.
 *
 * Requirements:
 *   - Accept a file and store it safely on the server.
 *
 * Security considerations:
 *   - Validate file type and size.
 *   - Sanitize filenames to prevent path traversal.
 *   - Avoid hardcoding credentials or sensitive paths (use environment).
 *   - Implement proper authentication / request validation.
 *   - Safe error handling — no internal details disclosed.
 *   - Uploaded files cannot be executed as server code.
 *
 * Build: gcc -Wall -Wextra -o p71_fc_c p71_fc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <limits.h>
#include <time.h>

/* -------------------------------------------------------------------------
 * Constants
 * ---------------------------------------------------------------------- */
#define MAX_FILENAME   256
#define MAX_PATH       512
#define MAX_EXT        16
#define MAX_FILE_BYTES (10L * 1024 * 1024)  /* 10 MB */

/* -------------------------------------------------------------------------
 * Allowlisted extensions — server-side; client MIME type is not trusted
 * ---------------------------------------------------------------------- */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "bmp", "webp",
    "pdf", "txt", "csv", "docx", "md",
    NULL
};

/* -------------------------------------------------------------------------
 * Internal helpers
 * ---------------------------------------------------------------------- */

/** Validate extension against allowlist; returns 1 if safe, 0 otherwise. */
static int file_type_allowed(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;
    char ext[MAX_EXT] = {0};
    const char *s = dot + 1;
    for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
        ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    for (int j = 0; ALLOWED_EXT[j] != NULL; j++)
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    return 0;
}

/**
 * sanitize_filename — strip directory components and replace unsafe chars.
 * Only [A-Za-z0-9._-] are kept; everything else becomes '_'.
 * Returns 1 on success, 0 for empty result or traversal tokens.
 */
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
    if (n == 0 || strcmp(out, ".") == 0 || strcmp(out, "..") == 0) return 0;
    return 1;
}

/**
 * make_stored_name — generate a random hex-based filename.
 * Prevents client-supplied names reaching the filesystem;
 * eliminates risk of stored files being executed via known names.
 */
static void make_stored_name(const char *ext, char *out, size_t out_size) {
    srand((unsigned int)time(NULL) ^ (unsigned int)(size_t)out);
    snprintf(out, out_size, "%08x%08x%08x%08x.%s",
             rand(), rand(), rand(), rand(), ext);
}

/**
 * safe_resolve — build absolute destination inside dir and verify
 * the resolved path cannot escape dir. Returns 1 on success, 0 on failure.
 */
static int safe_resolve(const char *dir, const char *filename,
                        char *dest, size_t dsz) {
    char dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;
    char resolved[PATH_MAX];
    if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
            >= (int)sizeof(resolved)) return 0;
    /* Prefix check */
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0 ||
            (resolved[dl] != '/' && resolved[dl] != '\0')) return 0;
    /* Reject remaining traversal tokens */
    if (strstr(resolved + dl, "..") != NULL) return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

/**
 * request_authenticated — validate API key from environment.
 * Returns 1 if key matches or no key is configured.
 */
static int request_authenticated(const char *supplied_key) {
    const char *expected = getenv("UPLOAD_API_KEY");
    if (!expected || *expected == '\0') return 1; /* key not set — open mode */
    return supplied_key && strcmp(supplied_key, expected) == 0;
}

/** Generic failure: set *err_msg and return 0. */
static int fail(const char **err_msg, const char *reason) {
    *err_msg = reason;
    return 0;
}

/* -------------------------------------------------------------------------
 * store_upload — core upload function
 *
 * Parameters:
 *   raw_name    : client-supplied filename (untrusted)
 *   data        : file content buffer
 *   data_len    : length in bytes
 *   upload_dir  : target directory (from env, not hardcoded)
 *   api_key     : caller-supplied API key for request validation
 *   err_msg     : set to safe message on both success and failure
 *
 * Steps:
 *  1. Authenticate the request.
 *  2. Validate presence and size of data.
 *  3. Sanitize the client-supplied filename.
 *  4. Validate file type against allowlist.
 *  5. Generate a server-controlled stored name.
 *  6. Create upload directory; resolve and verify destination path.
 *  7. Write file; handle I/O errors without leaking internal detail.
 * ---------------------------------------------------------------------- */
int store_upload(const char *raw_name, const unsigned char *data,
                 size_t data_len, const char *upload_dir,
                 const char *api_key, const char **err_msg) {

    /* Step 1 — authentication */
    if (!request_authenticated(api_key))
        return fail(err_msg, "Unauthorized.");

    /* Step 2 — input validation */
    if (!raw_name || !data || !upload_dir)
        return fail(err_msg, "Invalid input.");
    if (data_len == 0)
        return fail(err_msg, "Empty file.");
    if ((long)data_len > MAX_FILE_BYTES)
        return fail(err_msg, "File too large.");

    /* Step 3 — sanitize; never use raw client name on filesystem */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name)))
        return fail(err_msg, "Invalid filename.");

    /* Step 4 — type validation */
    if (!file_type_allowed(safe_name))
        return fail(err_msg, "File type not permitted.");

    /* Extract validated extension */
    char ext[MAX_EXT] = {0};
    const char *dot = strrchr(safe_name, '.');
    if (dot) {
        const char *s = dot + 1;
        for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
            ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }

    /* Step 5 — server-controlled filename; prevents execution as server code */
    char stored_name[MAX_FILENAME];
    make_stored_name(ext, stored_name, sizeof(stored_name));

    /* Step 6 — create dir; verify path is confined to upload_dir */
    if (mkdir(upload_dir, 0700) != 0) {
        struct stat st;
        if (stat(upload_dir, &st) != 0 || !S_ISDIR(st.st_mode))
            return fail(err_msg, "Upload service unavailable.");
    }

    char dest[MAX_PATH];
    if (!safe_resolve(upload_dir, stored_name, dest, sizeof(dest)))
        return fail(err_msg, "Upload rejected.");

    /* Step 7 — write; generic error on failure — no internal paths exposed */
    FILE *f = fopen(dest, "wb");
    if (!f) return fail(err_msg, "Upload failed.");

    size_t w     = fwrite(data, 1, data_len, f);
    int flush_ok = (fflush(f) == 0);
    fclose(f);

    if (w != data_len || !flush_ok) {
        remove(dest);
        return fail(err_msg, "Upload failed.");
    }

    *err_msg = "File uploaded successfully.";
    return 1;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || *upload_dir == '\0') upload_dir = "/tmp/uploads";
    const char *api_key = getenv("UPLOAD_API_KEY");

    const char *msg;
    const unsigned char data[] = "Secure upload demo content.";

    struct { const char *name; const char *label; } tests[] = {
        { "report.pdf",       "valid pdf"    },
        { "photo.jpg",        "valid jpg"    },
        { "script.php",       "bad type"     },
        { "back.sh",          "bad type 2"   },
        { "../../etc/passwd", "traversal"    },
        { "",                 "empty name"   },
        { NULL, NULL }
    };

    for (int i = 0; tests[i].name != NULL; i++) {
        int ok = store_upload(tests[i].name, data, sizeof(data) - 1,
                              upload_dir, api_key, &msg);
        printf("[%-12s] %s: %s\n", tests[i].label, ok ? "OK  " : "FAIL", msg);
    }
    return 0;
}
