#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <archive.h>
#include <archive_entry.h>
#include <limits.h>

// Helper to check for path traversal (Zip Slip)
int is_safe_path(const char *dest_dir, const char *entry_name) {
    char combined[PATH_MAX];
    char real_dest[PATH_MAX];
    char real_combined[PATH_MAX];

    snprintf(combined, sizeof(combined), "%s/%s", dest_dir, entry_name);

    if (realpath(dest_dir, real_dest) == NULL) return 0;
    
    // Note: In strict C, realpath requires the file to exist to resolve. 
    // For extraction, we check the parent or prefix.
    // Simplified safe check: reject ".." components or absolute paths in entry_name
    if (strstr(entry_name, "..") != NULL || entry_name[0] == '/') {
        return 0;
    }
    return 1;
}

void extract_archive(const char *filename) {
    struct archive *a;
    struct archive *ext;
    struct archive_entry *entry;
    int flags;
    int r;
    const char *dest_dir = "/tmp/unpack";

    // 1. Validate archive existence (archive_read_open_filename will fail if not found)
    
    // Use safe idiomatic libarchive pattern
    flags = ARCHIVE_EXTRACT_TIME;
    flags |= ARCHIVE_EXTRACT_PERM;
    flags |= ARCHIVE_EXTRACT_ACL;
    flags |= ARCHIVE_EXTRACT_FFLAGS;

    a = archive_read_new();
    archive_read_support_format_all(a);
    archive_read_support_filter_all(a); // Supports zip, tar, gzip, etc.
    
    ext = archive_write_disk_new();
    archive_write_disk_set_options(ext, flags);
    archive_write_disk_set_standard_lookup(ext);

    if ((r = archive_read_open_filename(a, filename, 10240))) {
        // Handle Open Error
        fprintf(stderr, "Failed to open archive: %s\n", archive_error_string(a));
        exit(1);
    }

    while (1) {
        r = archive_read_next_header(a, &entry);
        if (r == ARCHIVE_EOF)
            break;
        if (r < ARCHIVE_OK)
            fprintf(stderr, "%s\n", archive_error_string(a));
        if (r < ARCHIVE_WARN) {
            exit(1);
        }

        const char *currentFile = archive_entry_pathname(entry);
        
        // 2. Security Check: Prevent Zip Slip / arbitrary overwrite
        if (!is_safe_path(dest_dir, currentFile)) {
            fprintf(stderr, "Skipping unsafe entry: %s\n", currentFile);
            continue;
        }

        // Prepend destination directory
        char full_path[1024];
        snprintf(full_path, sizeof(full_path), "%s/%s", dest_dir, currentFile);
        archive_entry_set_pathname(entry, full_path);

        r = archive_read_extract(a, entry, flags);
        if (r != ARCHIVE_OK)
            fprintf(stderr, "%s\n", archive_error_string(a));
    }

    archive_read_close(a);
    archive_read_free(a);
    archive_write_close(ext);
    archive_write_free(ext);
}

int main() {
    extract_archive("archive.zip");
    return 0;
}
