#include <stdio.h>
#include <string.h>
#include <archive.h>
#include <archive_entry.h>

void safe_extract(const char *file) {
    struct archive *a = archive_read_new();
    struct archive *ext = archive_write_disk_new();
    struct archive_entry *entry;
    
    // 1. Validate
    archive_read_support_format_all(a);
    if (archive_read_open_filename(a, file, 10240) != ARCHIVE_OK) {
        // 4. Safety
        return;
    }

    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        const char *p = archive_entry_pathname(entry);
        
        // 2. Prevent Traversal
        // 3. Enforce /tmp/unpack
        if (strstr(p, "..") || strstr(p, "/etc/") || strncmp(p, "/", 1) == 0) {
            continue;
        }
        
        char full[512];
        snprintf(full, sizeof(full), "/tmp/unpack/%s", p);
        archive_entry_set_pathname(entry, full);
        
        if (archive_write_header(ext, entry) == ARCHIVE_OK) {
            archive_read_data_into_fd(a, 1);
            archive_write_finish_entry(ext);
        }
    }
    
    archive_read_free(a);
    archive_write_free(ext);
}
