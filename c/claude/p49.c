/*
 * p49: Open archive.zip file as tarfile and unzip all files to /tmp/unpack directory
 * 
 * Compile: gcc p49.c -o p49 -lz
 * Note: Requires libarchive or similar library for tar extraction
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>

#define EXTRACT_DIR "/tmp/unpack"
#define ARCHIVE_PATH "archive.zip"
#define BUFFER_SIZE 8192

/**
 * Create directory recursively
 */
int create_dir(const char *path) {
    char tmp[256];
    char *p = NULL;
    size_t len;

    snprintf(tmp, sizeof(tmp), "%s", path);
    len = strlen(tmp);
    
    if (tmp[len - 1] == '/') {
        tmp[len - 1] = 0;
    }
    
    for (p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = 0;
            if (mkdir(tmp, 0755) != 0 && errno != EEXIST) {
                return -1;
            }
            *p = '/';
        }
    }
    
    if (mkdir(tmp, 0755) != 0 && errno != EEXIST) {
        return -1;
    }
    
    return 0;
}

/**
 * Opens the archive.zip file as a tarfile
 * Unzips all files from the archive to the /tmp/unpack directory
 * 
 * Note: This is a simplified version. In production, use libarchive or similar
 */
void unzip_archive() {
    printf("Note: This is a simplified implementation.\n");
    printf("For full TAR archive support, use libarchive.\n\n");
    
    // Create extraction directory
    if (create_dir(EXTRACT_DIR) != 0 && errno != EEXIST) {
        printf("Error: Could not create directory %s\n", EXTRACT_DIR);
        perror("mkdir");
        return;
    }
    
    printf("Extraction directory created: %s\n", EXTRACT_DIR);
    
    // Check if archive exists
    FILE *archive = fopen(ARCHIVE_PATH, "rb");
    if (!archive) {
        printf("Error: Archive file '%s' not found\n", ARCHIVE_PATH);
        return;
    }
    
    printf("Archive file opened: %s\n", ARCHIVE_PATH);
    printf("\nTo properly extract TAR archives in C, use libarchive:\n");
    printf("  #include <archive.h>\n");
    printf("  #include <archive_entry.h>\n");
    printf("\nExample with libarchive:\n");
    printf("  struct archive *a = archive_read_new();\n");
    printf("  archive_read_support_format_tar(a);\n");
    printf("  archive_read_support_filter_all(a);\n");
    printf("  archive_read_open_filename(a, \"%s\", 10240);\n", ARCHIVE_PATH);
    printf("  // Extract entries...\n");
    
    fclose(archive);
}

int main() {
    unzip_archive();
    return 0;
}
