#include <stdio.h>
#include <archive.h>
#include <archive_entry.h>

// "With statement" equivalent in C is careful scope management or using blocks
void manage_resources() {
    struct archive *a;
    int r;

    // 1. Resources properly opened/closed
    a = archive_read_new();
    archive_read_support_filter_all(a); // 3. Compressed files handled
    archive_read_support_format_all(a);

    // 2. Safely read
    r = archive_read_open_filename(a, "archive.tar", 10240);
    if (r != ARCHIVE_OK) {
        // 4. Errors managed
        fprintf(stderr, "Error opening\n");
    } else {
         struct archive_entry *entry;
         while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
             printf("%s\n", archive_entry_pathname(entry));
             archive_read_data_skip(a);
         }
    }

    // 1. Close resources
    archive_read_close(a);
    archive_read_free(a);
}
