#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <archive.h>
#include <archive_entry.h>

// Helper check for safe path
int is_subpath(const char *base, const char *path) {
    char real_base[PATH_MAX];
    char real_path[PATH_MAX];

    // Note: realpath usually requires existence. 
    // We assume base exists. We construct full path and check expected prefix.
    // For non-existent files during extraction, we sanitize component-wise.
    // Here we perform basic ".." rejection string check for demo.
    if (strstr(path, "..")) return 0;
    if (path[0] == '/') return 0; // Absolute path in archive
    return 1;
}

void safe_extract(const char *filename, const char *dest_dir) {
    struct archive *a;
    struct archive *ext;
    struct archive_entry *entry;
    int flags;
    int r;

    a = archive_read_new();
    archive_read_support_format_all(a);
    archive_read_support_filter_all(a);
    
    ext = archive_write_disk_new();
    flags = ARCHIVE_EXTRACT_TIME | ARCHIVE_EXTRACT_PERM | ARCHIVE_EXTRACT_ACL | ARCHIVE_EXTRACT_FFLAGS;
    archive_write_disk_set_options(ext, flags);

    // Validate archive exists
    if ((r = archive_read_open_filename(a, filename, 10240))) {
        // Handle error gracefully
        fprintf(stderr, "Error opening archive: %s\n", archive_error_string(a));
        exit(1);
    }

    while (1) {
        r = archive_read_next_header(a, &entry);
        if (r == ARCHIVE_EOF) break;
        if (r < ARCHIVE_OK) fprintf(stderr, "%s\n", archive_error_string(a));
        if (r < ARCHIVE_WARN) exit(1);

        const char *current_file = archive_entry_pathname(entry);
        char full_entry_path[2048];
        snprintf(full_entry_path, sizeof(full_entry_path), "%s/%s", dest_dir, current_file);

        // Security check
        if (!is_subpath(dest_dir, current_file)) {
            printf("Skipping unsafe entry: %s\n", current_file);
            continue;
        }

        archive_entry_set_pathname(entry, full_entry_path);
        
        r = archive_read_extract(a, entry, flags);
        if (r != ARCHIVE_OK) {
             fprintf(stderr, "%s\n", archive_error_string(a));
        }
    }

    archive_read_close(a);
    archive_read_free(a);
    archive_write_close(ext);
    archive_write_free(ext);
}

int main() {
    safe_extract("archive.zip", "/tmp/unpack");
    return 0;
}
