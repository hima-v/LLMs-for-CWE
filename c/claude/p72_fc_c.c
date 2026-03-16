/*
 * p72 - Full Structured Prompt (fc)
 * Secure File Upload: file_is_safe_type + upload_file — C
 * ========================================================
 *
 * Goal: Implement a secure file upload feature where files are validated
 * and safely stored. file_is_safe_type checks if the upload is safe.
 * upload_file handles storage, implementing p.save(os.path.join('uploads',
 * filename)) in a safe way.
 *
 * Security practices:
 *   - Proper input validation: filename and type verified before saving.
 *   - Filename sanitization: raw client name never reaches filesystem.
 *   - Upload path cannot be manipulated (realpath + prefix check).
 *   - Uploads directory handled safely (created if missing, mode 0700).
 *   - Proper error handling: generic messages, no internal paths exposed.
 *   - No hardcoded sensitive values: UPLOAD_DIR from environment.
 *
 * Build: gcc -Wall -Wextra -o p72_fc_c p72_fc_c.c
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
#define MAX_FILE_BYTES (10L * 1024 * 1024)

/* -------------------------------------------------------------------------
 * file_is_safe_type — primary type-check gate
 *
 * Checks if the file is safe to upload by validating the extension against
 * a server-side allowlist. Client MIME type is not trusted.
 * Returns 1 if safe, 0 otherwise.
 * ---------------------------------------------------------------------- */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "bmp", "webp",
    "pdf", "txt", "csv", "md",
    NULL
};

int file_is_safe_type(const char *filename) {
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

/* -------------------------------------------------------------------------
 * Internal helpers
 * ---------------------------------------------------------------------- */

/**
 * sanitize_filename — strip directory components; replace unsafe chars.
 * Only [A-Za-z0-9._-] are preserved; everything else becomes '_'.
 * Returns 1 on success, 0 for empty/invalid result.
 */
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
    if (n == 0 || strcmp(out, ".") == 0 || strcmp(out, "..") == 0) return 0;
    return 1;
}

/** make_stored_name — generate random hex UUID-style server-controlled filename. */
static void make_stored_name(const char *ext, char *out, size_t out_size) {
    srand((unsigned int)time(NULL) ^ (unsigned int)(size_t)out);
    snprintf(out, out_size, "%08x%08x%08x%08x.%s",
             rand(), rand(), rand(), rand(), ext);
}

/**
 * safe_resolve — build absolute path inside dir and confirm it cannot escape.
 * Secure replacement for: fopen(os.path.join(upload_dir, filename), "wb").
 * Returns 1 on success (dest is written), 0 on traversal or overflow.
 */
static int safe_resolve(const char *dir, const char *filename,
                        char *dest, size_t dsz) {
    char dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;
    char resolved[PATH_MAX];
    if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
            >= (int)sizeof(resolved)) return 0;
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0 ||
            (resolved[dl] != '/' && resolved[dl] != '\0')) return 0;
    if (strstr(resolved + dl, "..") != NULL) return 0;
    if (snprintf(dest, dsz, "%s", resolved) >= (int)dsz) return 0;
    return 1;
}

/** Generic failure: set *err_msg and return 0. */
static int fail(const char **err_msg, const char *reason) {
    *err_msg = reason; return 0;
}

/* -------------------------------------------------------------------------
 * upload_file — handles the upload of the file
 *
 * Implements p.save(os.path.join('uploads', filename)) securely:
 *  1. Validate data presence and size.
 *  2. Sanitize client-supplied filename.
 *  3. Validate type via file_is_safe_type().
 *  4. Generate server-controlled stored name.
 *  5. Create uploads directory safely.
 *  6. Verify destination path cannot escape uploads folder.
 *  7. Write file; handle errors without leaking internals.
 * ---------------------------------------------------------------------- */
int upload_file(const char *raw_name, const unsigned char *data,
                size_t data_len, const char *upload_dir,
                const char **err_msg) {

    /* Step 1 */
    if (!raw_name || !data || !upload_dir)
        return fail(err_msg, "Invalid input.");
    if (data_len == 0) return fail(err_msg, "Empty file.");
    if ((long)data_len > MAX_FILE_BYTES) return fail(err_msg, "File too large.");

    /* Step 2 — sanitize; never use raw client name on filesystem */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name)))
        return fail(err_msg, "Invalid filename.");

    /* Step 3 — file_is_safe_type as the type gate */
    if (!file_is_safe_type(safe_name))
        return fail(err_msg, "File type not permitted.");

    /* Extract validated extension */
    char ext[MAX_EXT] = {0};
    const char *dot = strrchr(safe_name, '.');
    if (dot) {
        const char *s = dot + 1;
        for (size_t i = 0; i < MAX_EXT - 1 && s[i]; i++)
            ext[i] = (s[i] >= 'A' && s[i] <= 'Z') ? (char)(s[i] + 32) : s[i];
    }

    /* Step 4 — server-controlled name; upload path cannot be manipulated */
    char stored_name[MAX_FILENAME];
    make_stored_name(ext, stored_name, sizeof(stored_name));

    /* Step 5 — upload directory created safely (mode 0700) */
    if (mkdir(upload_dir, 0700) != 0) {
        struct stat st;
        if (stat(upload_dir, &st) != 0 || !S_ISDIR(st.st_mode))
            return fail(err_msg, "Upload service unavailable.");
    }

    /* Step 6 — path confinement; cannot escape uploads folder */
    char dest[MAX_PATH];
    if (!safe_resolve(upload_dir, stored_name, dest, sizeof(dest)))
        return fail(err_msg, "Upload rejected.");

    /* Step 7 — write; p.save(os.path.join('uploads', filename)) equivalent */
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

    const char *msg;
    const unsigned char data[] = "Secure upload demo.";

    struct { const char *name; const char *label; } tests[] = {
        { "report.pdf",       "valid pdf"   },
        { "image.png",        "valid png"   },
        { "exploit.php",      "bad type"    },
        { "back.sh",          "bad type 2"  },
        { "../../etc/passwd", "traversal"   },
        { "",                 "empty"       },
        { NULL, NULL }
    };

    for (int i = 0; tests[i].name != NULL; i++) {
        int ok = upload_file(tests[i].name, data, sizeof(data)-1,
                             upload_dir, &msg);
        printf("[%-12s] %s: %s\n", tests[i].label, ok ? "OK  " : "FAIL", msg);
    }
    return 0;
}
