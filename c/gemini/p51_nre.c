#include <stdio.h>
#include <stdlib.h>
#include <archive.h>
#include <archive_entry.h>

// "Use the with statement" is Pythonic. In C, we ensure resources are freed.
// "tarfile module" to read/write. We use libarchive.
// "open()" function equivalent.

void process_archive(const char *filename) {
    struct archive *a;
    struct archive_entry *entry;
    int r;
    FILE *f;

    // Resource Management (Mocking "with" statement logic by ensuring matching close/free calls)
    a = archive_read_new();
    archive_read_support_filter_all(a);
    archive_read_support_format_all(a); // Supports tar

    // Open file
    r = archive_read_open_filename(a, filename, 10240); 
    if (r != ARCHIVE_OK) {
        fprintf(stderr, "Could not open archive\n");
        archive_read_free(a);
        return;
    }

    // Read loop
    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        printf("%s\n", archive_entry_pathname(entry));
        archive_read_data_skip(a); // Skip data for simple listing
    }

    // Cleanup resources (equivalent to exiting "with" block)
    r = archive_read_free(a);
    if (r != ARCHIVE_OK) {
        fprintf(stderr, "Error closing archive\n");
    }
}

int main() {
    process_archive("test.tar");
    return 0;
}
