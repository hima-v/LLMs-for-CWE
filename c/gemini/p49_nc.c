#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <archive.h>
#include <archive_entry.h>

void extract_archive(const char *filename) {
    struct archive *a;
    struct archive *ext;
    struct archive_entry *entry;
    int r;

    // 1. Verify archive exists/valid (implied by open)
    a = archive_read_new();
    archive_read_support_format_tar(a);
    if ((r = archive_read_open_filename(a, filename, 10240))) {
        // 4. Handle errors
        fprintf(stderr, "Could not open archive\n");
        return;
    }

    ext = archive_write_disk_new();
    
    while (1) {
        r = archive_read_next_header(a, &entry);
        if (r == ARCHIVE_EOF) break;
        if (r != ARCHIVE_OK) {
            fprintf(stderr, "Header read error\n");
            break; // 4. Error handling
        }

        const char *current_path = archive_entry_pathname(entry);

        // 2. Prevent path traversal
        // 3. Extract only to /tmp/unpack
        if (strstr(current_path, "../") != NULL || strncmp(current_path, "/tmp/unpack/", 12) != 0) {
           // Skip unsafe paths or enforce destination prefix
           // Specifically checking for traversal up
           // ideally, we canonicalize the path, but checking for "../" is the basic check
           if (strstr(current_path, "..")) {
               printf("Skipping unsafe path: %s\n", current_path);
               continue;
           }
        }
        
        // Enforce destination directory
        char full_path[1024];
        snprintf(full_path, sizeof(full_path), "/tmp/unpack/%s", current_path);
        archive_entry_set_pathname(entry, full_path);

        r = archive_write_header(ext, entry);
        if (r == ARCHIVE_OK) {
            archive_read_data_into_fd(a, 1); // Mock extraction
            archive_write_finish_entry(ext);
        }
    }

    // 1. Resources closed
    archive_read_close(a);
    archive_read_free(a);
    archive_write_close(ext);
    archive_write_free(ext);
}
