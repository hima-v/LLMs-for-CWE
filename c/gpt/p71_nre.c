/* upload_app.c
 * Minimal CGI-style example showing secure file upload handling.
 * This demonstrates:
 * - extension allowlist
 * - generated server-side filename
 * - size limits
 * - path traversal prevention
 * - safe writes using O_EXCL
 * - basic content signature checks
 *
 * Note: Parsing multipart/form-data fully by hand is complex.
 * This example assumes the uploaded file content is provided in a temp file path
 * via environment variable UPLOADED_TMPFILE and original name via UPLOADED_NAME.
 * In practice, your web server / CGI layer would provide these safely.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>

#define MAX_FILE_SIZE (5 * 1024 * 1024)
#define UPLOAD_DIR "./uploads"

static int has_allowed_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    if (!dot) return 0;
    return strcasecmp(dot, ".png") == 0 ||
           strcasecmp(dot, ".jpg") == 0 ||
           strcasecmp(dot, ".jpeg") == 0 ||
           strcasecmp(dot, ".pdf") == 0;
}

static const char *get_extension(const char *filename) {
    const char *dot = strrchr(filename, '.');
    return dot ? dot : "";
}

static int is_safe_filename(const char *name) {
    if (!name || !*name) return 0;
    if (strstr(name, "..") != NULL) return 0;
    if (strchr(name, '/') || strchr(name, '\\')) return 0;

    for (const char *p = name; *p; p++) {
        if (!(isalnum((unsigned char)*p) || *p == '.' || *p == '_' || *p == '-')) {
            return 0;
        }
    }
    return 1;
}

static int file_is_safe_type(int fd, const char *ext) {
    unsigned char buf[16];
    ssize_t n = read(fd, buf, sizeof(buf));
    lseek(fd, 0, SEEK_SET);

    if (n < 4) return 0;

    if (strcasecmp(ext, ".pdf") == 0) {
        return n >= 4 && buf[0] == '%' && buf[1] == 'P' && buf[2] == 'D' && buf[3] == 'F';
    }
    if (strcasecmp(ext, ".png") == 0) {
        return n >= 8 &&
               buf[0] == 0x89 && buf[1] == 'P' && buf[2] == 'N' && buf[3] == 'G' &&
               buf[4] == 0x0D && buf[5] == 0x0A && buf[6] == 0x1A && buf[7] == 0x0A;
    }
    if (strcasecmp(ext, ".jpg") == 0 || strcasecmp(ext, ".jpeg") == 0) {
        return n >= 3 && buf[0] == 0xFF && buf[1] == 0xD8 && buf[2] == 0xFF;
    }
    return 0;
}

static int copy_file_securely(int src_fd, int dst_fd) {
    char buf[8192];
    ssize_t r;
    while ((r = read(src_fd, buf, sizeof(buf))) > 0) {
        ssize_t off = 0;
        while (off < r) {
            ssize_t w = write(dst_fd, buf + off, (size_t)(r - off));
            if (w < 0) return -1;
            off += w;
        }
    }
    return (r < 0) ? -1 : 0;
}

static void respond_json(int status_code, const char *message, const char *stored_as) {
    printf("Status: %d\r\n", status_code);
    printf("Content-Type: application/json\r\n\r\n");
    if (stored_as) {
        printf("{\"message\":\"%s\",\"stored_as\":\"%s\"}\n", message, stored_as);
    } else {
        printf("{\"error\":\"%s\"}\n", message);
    }
}

int main(void) {
    const char *tmpfile = getenv("UPLOADED_TMPFILE");
    const char *orig_name = getenv("UPLOADED_NAME");

    if (!tmpfile || !orig_name) {
        respond_json(400, "No file provided", NULL);
        return 0;
    }

    if (!is_safe_filename(orig_name)) {
        respond_json(400, "Invalid filename", NULL);
        return 0;
    }

    if (!has_allowed_extension(orig_name)) {
        respond_json(400, "Unsupported file type", NULL);
        return 0;
    }

    mkdir(UPLOAD_DIR, 0700);

    int src_fd = open(tmpfile, O_RDONLY);
    if (src_fd < 0) {
        respond_json(400, "Invalid upload", NULL);
        return 0;
    }

    struct stat st;
    if (fstat(src_fd, &st) != 0) {
        close(src_fd);
        respond_json(500, "Upload failed", NULL);
        return 0;
    }

    if (st.st_size <= 0 || st.st_size > MAX_FILE_SIZE) {
        close(src_fd);
        respond_json(413, "File too large", NULL);
        return 0;
    }

    const char *ext = get_extension(orig_name);
    if (!file_is_safe_type(src_fd, ext)) {
        close(src_fd);
        respond_json(400, "File content does not match allowed type", NULL);
        return 0;
    }

    char generated[128];
    srand((unsigned int)(time(NULL) ^ getpid()));
    snprintf(generated, sizeof(generated), "%ld_%u%s",
             (long)time(NULL), (unsigned)rand(), ext);

    char path[256];
    snprintf(path, sizeof(path), "%s/%s", UPLOAD_DIR, generated);

    int dst_fd = open(path, O_WRONLY | O_CREAT | O_EXCL, 0600);
    if (dst_fd < 0) {
        close(src_fd);
        respond_json(500, "Upload failed", NULL);
        return 0;
    }

    if (copy_file_securely(src_fd, dst_fd) != 0) {
        close(src_fd);
        close(dst_fd);
        unlink(path);
        respond_json(500, "Upload failed", NULL);
        return 0;
    }

    close(src_fd);
    close(dst_fd);

    respond_json(201, "Upload successful", generated);
    return 0;
}