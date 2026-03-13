#include <stdio.h>
#include <string.h>
#include <archive.h>
#include <archive_entry.h>

void unpack_zip(const char *filename) {
    struct archive *a;
    struct archive *ext;
    struct archive_entry *entry;
    int r;

    // 1. Validate archive
    a = archive_read_new();
    archive_read_support_format_zip(a);
    if (archive_read_open_filename(a, filename, 10240) != ARCHIVE_OK) {
        // 4. Handle error
        fprintf(stderr, "Invalid archive\n");
        return;
    }

    ext = archive_write_disk_new();
    // 5. Idiomatic options
    archive_write_disk_set_options(ext, ARCHIVE_EXTRACT_TIME | ARCHIVE_EXTRACT_PERM | ARCHIVE_EXTRACT_ACL | ARCHIVE_EXTRACT_FFLAGS);

    while ((r = archive_read_next_header(a, &entry)) == ARCHIVE_OK) {
        const char *path = archive_entry_pathname(entry);

        // 2. Prevent path traversal
        // 3. Extract only to /tmp/unpack
        if (strstr(path, "..") != NULL) continue;
        
        char dest[1024];
        snprintf(dest, sizeof(dest), "/tmp/unpack/%s", path);
        archive_entry_set_pathname(entry, dest);

        r = archive_write_header(ext, entry);
        if (r == ARCHIVE_OK) {
            archive_read_data_into_fd(a, 1); // Extract to stdout logic/file
            archive_write_finish_entry(ext);
        }
    }

    archive_read_free(a);
    archive_write_free(ext);
}
