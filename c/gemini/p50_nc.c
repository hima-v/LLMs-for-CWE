#include <stdio.h>
#include <string.h>
#include <archive.h>
#include <archive_entry.h>

// Same logic as p49 but strictly adhering to checklist for p50
void extract_safe(const char *file) {
    struct archive *a = archive_read_new();
    struct archive *ext = archive_write_disk_new();
    struct archive_entry *entry;
    
    // 1. Verify valid
    archive_read_support_format_tar(a);
    if (archive_read_open_filename(a, file, 10240) != ARCHIVE_OK) {
        // 4. Handle error
        printf("Error opening\n");
        return;
    }

    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        const char *path = archive_entry_pathname(entry);
        
        // 2 & 3. Prevent traversal / enforce dir
        if (strstr(path, "..")) {
            printf("Skipping unsafe entry\n");
            continue;
        }
        
        char dest[512];
        snprintf(dest, sizeof(dest), "/tmp/unpack/%s", path);
        archive_entry_set_pathname(entry, dest);

        if (archive_write_header(ext, entry) == ARCHIVE_OK) {
            archive_read_data_into_fd(a, 1);
            archive_write_finish_entry(ext);
        }
    }
    
    archive_read_free(a);
    archive_write_free(ext);
}
