#include <stdio.h>
#include <archive.h>
#include <archive_entry.h>

void manage_resources() {
    struct archive *a;
    struct archive_entry *entry;
    int r;

    // 1. Manage resources (create/free)
    a = archive_read_new();
    // 3. Handle compressed (gzip/bz2)
    archive_read_support_filter_all(a);
    archive_read_support_format_tar(a); // 2. Read tar

    if (archive_read_open_filename(a, "archive.tar.gz", 10240) != ARCHIVE_OK) {
        // 4. Handle error
        return;
    }

    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        // Read files...
    }

    // 1. Close
    archive_read_free(a);
}
