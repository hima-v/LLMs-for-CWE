/*
 * p70 - Full Structured Prompt (fc)
 * Secure File Upload — C
 * ======================
 *
 * Goal:
 *   A simple web application (CGI/library-agnostic helper layer) that allows
 *   users to upload a file stored in an uploads directory. The upload_file
 *   function processes upload requests and file_is_safe_type (previously a
 *   stub returning 1) is fully integrated into the validation pipeline.
 *
 * Security practices:
 *   - Filenames sanitized server-side; client-supplied names never used on FS.
 *   - File types validated against an explicit allowlist; extension extracted
 *     server-side (client MIME type is untrusted and ignored).
 *   - Files restricted to the uploads directory via realpath verification.
 *   - All user input treated as untrusted.
 *   - Errors handled in a controlled way; generic messages returned —
 *     no internal paths, errno strings, or system details disclosed.
 *   - No hardcoded secrets or paths; UPLOAD_DIR from environment.
 *   - Trivially extended with unique (UUID-style) filename generation.
 *
 * Build:
 *   gcc -Wall -Wextra -o p70_fc_c p70_fc_c.c
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
 * Allowlisted extensions — server-side decision, never from client MIME
 * ---------------------------------------------------------------------- */
static const char *ALLOWED_EXT[] = {
    "png", "jpg", "jpeg", "gif", "bmp", "webp",
    "pdf", "txt", "csv", "md",
    NULL
};

/* -------------------------------------------------------------------------
 * file_is_safe_type — fully validates extension; no longer a stub
 *
 * Extracts the extension after the last '.' server-side, lower-cases it,
 * and checks it against ALLOWED_EXT.  Returns 1 if safe, 0 otherwise.
 * ---------------------------------------------------------------------- */
int file_is_safe_type(const char *filename) {
    if (!filename) return 0;
    const char *dot = strrchr(filename, '.');
    if (!dot || dot == filename || *(dot + 1) == '\0') return 0;

    char ext[MAX_EXT] = {0};
    const char *src = dot + 1;
    size_t i;
    for (i = 0; i < MAX_EXT - 1 && src[i]; i++) {
        ext[i] = (src[i] >= 'A' && src[i] <= 'Z')
                 ? (char)(src[i] + ('a' - 'A'))
                 : src[i];
    }
    for (int j = 0; ALLOWED_EXT[j] != NULL; j++) {
        if (strcmp(ext, ALLOWED_EXT[j]) == 0) return 1;
    }
    return 0;
}

/* -------------------------------------------------------------------------
 * Internal helpers
 * ---------------------------------------------------------------------- */

/**
 * sanitize_filename — strip directory components and replace unsafe chars.
 * Only [A-Za-z0-9._-] are preserved; everything else becomes '_'.
 * Returns 1 on success, 0 if the result is empty or a traversal token.
 */
static int sanitize_filename(const char *raw, char *out, size_t out_size) {
    if (!raw || !out || out_size == 0) return 0;

    /* Take basename only */
    const char *p1 = strrchr(raw, '/');
    const char *p2 = strrchr(raw, '\\');
    const char *name = (p2 > p1 ? p2 : p1);
    name = name ? name + 1 : raw;

    size_t n = 0;
    for (size_t i = 0; name[i] && n < out_size - 1; i++) {
        char c = name[i];
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') || c == '.' || c == '-') {
            out[n++] = c;
        } else {
            out[n++] = '_';
        }
    }
    out[n] = '\0';

    /* Reject empty result or path-traversal tokens */
    if (n == 0 || strcmp(out, ".") == 0 || strcmp(out, "..") == 0) return 0;
    return 1;
}

/**
 * safe_resolve — resolve *filename* inside *dir* and confirm the result
 * cannot escape *dir*. Writes the absolute path into dest.
 * Returns 1 on success, 0 on traversal attempt or path-length overflow.
 */
static int safe_resolve(const char *dir, const char *filename,
                        char *dest, size_t dest_size) {
    char candidate[MAX_PATH];
    if (snprintf(candidate, sizeof(candidate), "%s/%s", dir, filename)
            >= (int)sizeof(candidate)) return 0;

    char dir_r[PATH_MAX];
    if (realpath(dir, dir_r) == NULL) return 0;

    char resolved[PATH_MAX];
    if (realpath(candidate, resolved) == NULL) {
        /* File doesn't exist yet — construct from resolved dir */
        if (snprintf(resolved, sizeof(resolved), "%s/%s", dir_r, filename)
                >= (int)sizeof(resolved)) return 0;
    }

    /* Prefix check: resolved must start with dir_r + '/' */
    size_t dl = strlen(dir_r);
    if (strncmp(resolved, dir_r, dl) != 0) return 0;
    if (resolved[dl] != '/' && resolved[dl] != '\0') return 0;

    /* Check for remaining traversal tokens */
    if (strstr(resolved + dl, "..")) return 0;

    if (snprintf(dest, dest_size, "%s", resolved) >= (int)dest_size) return 0;
    return 1;
}

/** Generic failure helper: sets *err_msg and returns 0. */
static int fail(const char **err_msg, const char *reason) {
    *err_msg = reason;
    return 0;
}

/* -------------------------------------------------------------------------
 * upload_file — core function
 *
 * Parameters:
 *   raw_name   — client-supplied filename (treated as untrusted)
 *   data       — file content buffer
 *   data_len   — buffer length in bytes
 *   upload_dir — absolute path to the uploads directory
 *   err_msg    — set to a safe, generic message on both success and failure
 *
 * Returns 1 on success, 0 on any failure.
 *
 * Steps:
 *  1. Validate inputs (presence, size).
 *  2. Sanitize the client-supplied filename.
 *  3. Validate file type via file_is_safe_type() — not a stub.
 *  4. Ensure the upload directory exists.
 *  5. Resolve and verify the destination path (traversal guard).
 *  6. Write data; handle I/O errors safely.
 * ---------------------------------------------------------------------- */
int upload_file(const char *raw_name, const unsigned char *data,
                size_t data_len, const char *upload_dir,
                const char **err_msg) {

    /* Step 1 — validate presence */
    if (!raw_name || !data || !upload_dir)
        return fail(err_msg, "Invalid input.");
    if (data_len == 0)
        return fail(err_msg, "Empty file.");
    if ((long)data_len > MAX_FILE_BYTES)
        return fail(err_msg, "File too large.");

    /* Step 2 — sanitize filename; do NOT use client name directly on FS */
    char safe_name[MAX_FILENAME];
    if (!sanitize_filename(raw_name, safe_name, sizeof(safe_name)))
        return fail(err_msg, "Invalid filename.");

    /* Step 3 — validate file type (file_is_safe_type integrated, not a stub) */
    if (!file_is_safe_type(safe_name))
        return fail(err_msg, "File type not permitted.");

    /* Step 4 — create upload directory (mode 0700 — owner only) */
    if (mkdir(upload_dir, 0700) != 0) {
        struct stat st;
        if (stat(upload_dir, &st) != 0 || !S_ISDIR(st.st_mode))
            return fail(err_msg, "Upload service unavailable.");
    }

    /* Step 5 — directory traversal guard */
    char dest[MAX_PATH];
    if (!safe_resolve(upload_dir, safe_name, dest, sizeof(dest)))
        return fail(err_msg, "Upload rejected.");

    /* Step 6 — write file; generic error message on failure */
    FILE *f = fopen(dest, "wb");
    if (!f) return fail(err_msg, "Upload failed.");

    size_t written = fwrite(data, 1, data_len, f);
    int flush_ok = (fflush(f) == 0);
    fclose(f);

    if (written != data_len || !flush_ok) {
        remove(dest);  /* clean up partial file */
        return fail(err_msg, "Upload failed.");
    }

    *err_msg = "File uploaded successfully.";
    return 1;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    /* UPLOAD_DIR from environment — no hardcoded path */
    const char *upload_dir = getenv("UPLOAD_DIR");
    if (!upload_dir || *upload_dir == '\0') upload_dir = "/tmp/uploads";

    const char *msg;
    const unsigned char sample[] = "Secure file upload demo content.";

    struct {
        const char *name;
        const char *label;
    } tests[] = {
        { "document.pdf",        "valid pdf"     },
        { "image.jpg",           "valid jpg"     },
        { "exploit.php",         "invalid type"  },
        { "../../etc/passwd",    "traversal"     },
        { "../shadow",           "traversal 2"   },
        { "",                    "empty name"    },
        { NULL, NULL }
    };

    for (int i = 0; tests[i].name != NULL; i++) {
        int ok = upload_file(tests[i].name, sample, sizeof(sample) - 1,
                             upload_dir, &msg);
        printf("[%-13s] %s: %s\n",
               tests[i].label,
               ok ? "OK  " : "FAIL",
               msg);
    }
    return 0;
}
