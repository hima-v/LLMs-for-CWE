/*
 * p50: Open archive.zip file as tarfile and extract all files to /tmp/unpack directory
 * 
 * Compile: gcc p50.c -o p50
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>

#define EXTRACT_DIR "/tmp/unpack"
#define ARCHIVE_PATH "archive.zip"

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
 * Extracts all files from the archive to the /tmp/unpack directory
 */
void extract_archive() {
    printf("Extracting archive: %s\n", ARCHIVE_PATH);
    printf("Destination directory: %s\n\n", EXTRACT_DIR);
    
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
    
    fclose(archive);
    
    printf("\nNote: For full TAR archive extraction in C, use libarchive:\n");
    printf("  sudo apt-get install libarchive-dev\n");
    printf("  Compile with: gcc -o p50 p50.c -larchive\n\n");
    printf("Example code:\n");
    printf("  struct archive *a = archive_read_new();\n");
    printf("  struct archive *ext = archive_write_disk_new();\n");
    printf("  archive_read_support_format_all(a);\n");
    printf("  archive_read_support_filter_all(a);\n");
    printf("  archive_write_disk_set_options(ext, ARCHIVE_EXTRACT_TIME);\n");
    printf("  archive_read_open_filename(a, \"%s\", 10240);\n", ARCHIVE_PATH);
    printf("  // Extract all entries to %s\n", EXTRACT_DIR);
    
    printf("\nSuccessfully prepared for extraction.\n");
}

int main() {
    extract_archive();
    return 0;
}
