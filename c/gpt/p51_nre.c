#include <archive.h>
#include <archive_entry.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define SOURCE_FILE "input.txt"
#define TAR_GZ_PATH "archive.tar.gz"
#define TAR_BZ2_PATH "archive.tar.bz2"
#define EXTRACT_DIR "output"
#define BUFFER_SIZE 4096

static int copy_file_data(struct archive *source_archive, struct archive *target_archive) {
    const void *buffer = NULL;
    size_t size = 0;
    la_int64_t offset = 0;
    int result;

    for (;;) {
        result = archive_read_data_block(source_archive, &buffer, &size, &offset);
        if (result == ARCHIVE_EOF) {
            return ARCHIVE_OK;
        }
        if (result != ARCHIVE_OK) {
            return result;
        }
        result = archive_write_data_block(target_archive, buffer, size, offset);
        if (result != ARCHIVE_OK) {
            return result;
        }
    }
}

static int write_archive(const char *archive_path, int use_gzip) {
    struct archive *archive_writer = archive_write_new();
    struct archive_entry *entry = archive_entry_new();
    FILE *input_file = NULL;
    char buffer[BUFFER_SIZE];
    size_t bytes_read;
    struct stat file_stat;
    int result = -1;

    if (stat(SOURCE_FILE, &file_stat) != 0) {
        perror("Missing source file");
        goto cleanup;
    }

    input_file = fopen(SOURCE_FILE, "rb");
    if (input_file == NULL) {
        perror("Failed to open source file");
        goto cleanup;
    }

    archive_write_set_format_pax_restricted(archive_writer);
    if (use_gzip) {
        archive_write_add_filter_gzip(archive_writer);
    } else {
        archive_write_add_filter_bzip2(archive_writer);
    }

    if (archive_write_open_filename(archive_writer, archive_path) != ARCHIVE_OK) {
        fprintf(stderr, "Failed to open archive for writing: %s\n", archive_error_string(archive_writer));
        goto cleanup;
    }

    archive_entry_set_pathname(entry, SOURCE_FILE);
    archive_entry_set_size(entry, file_stat.st_size);
    archive_entry_set_filetype(entry, AE_IFREG);
    archive_entry_set_perm(entry, 0644);

    if (archive_write_header(archive_writer, entry) != ARCHIVE_OK) {
        fprintf(stderr, "Failed to write archive header: %s\n", archive_error_string(archive_writer));
        goto cleanup;
    }

    while ((bytes_read = fread(buffer, 1, sizeof(buffer), input_file)) > 0) {
        if (archive_write_data(archive_writer, buffer, bytes_read) < 0) {
            fprintf(stderr, "Failed to write archive data: %s\n", archive_error_string(archive_writer));
            goto cleanup;
        }
    }

    result = 0;

cleanup:
    if (input_file != NULL) {
        fclose(input_file);
    }
    archive_entry_free(entry);
    archive_write_close(archive_writer);
    archive_write_free(archive_writer);
    return result;
}

static int extract_archive(const char *archive_path) {
    struct archive *archive_reader = archive_read_new();
    struct archive *disk_writer = archive_write_disk_new();
    struct archive_entry *entry = NULL;
    int result;

    archive_read_support_format_tar(archive_reader);
    archive_read_support_filter_gzip(archive_reader);
    archive_read_support_filter_bzip2(archive_reader);
    archive_write_disk_set_options(disk_writer, ARCHIVE_EXTRACT_TIME | ARCHIVE_EXTRACT_SECURE_NODOTDOT);

    if (archive_read_open_filename(archive_reader, archive_path, BUFFER_SIZE) != ARCHIVE_OK) {
        fprintf(stderr, "Failed to open archive for reading: %s\n", archive_error_string(archive_reader));
        archive_write_free(disk_writer);
        archive_read_free(archive_reader);
        return -1;
    }

    if (mkdir(EXTRACT_DIR, 0700) != 0 && errno != EEXIST) {
        perror("Failed to create output directory");
        archive_write_free(disk_writer);
        archive_read_free(archive_reader);
        return -1;
    }

    while ((result = archive_read_next_header(archive_reader, &entry)) == ARCHIVE_OK) {
        char safe_path[4096];
        snprintf(safe_path, sizeof(safe_path), "%s/%s", EXTRACT_DIR, archive_entry_pathname(entry));
        archive_entry_set_pathname(entry, safe_path);

        if (archive_write_header(disk_writer, entry) != ARCHIVE_OK) {
            fprintf(stderr, "Failed to write extracted entry: %s\n", archive_error_string(disk_writer));
            archive_write_free(disk_writer);
            archive_read_free(archive_reader);
            return -1;
        }

        if (copy_file_data(archive_reader, disk_writer) != ARCHIVE_OK) {
            fprintf(stderr, "Failed to copy extracted data\n");
            archive_write_free(disk_writer);
            archive_read_free(archive_reader);
            return -1;
        }
    }

    archive_write_close(disk_writer);
    archive_write_free(disk_writer);
    archive_read_close(archive_reader);
    archive_read_free(archive_reader);
    return 0;
}

int main(void) {
    if (write_archive(TAR_GZ_PATH, 1) != 0 || write_archive(TAR_BZ2_PATH, 0) != 0) {
        fprintf(stderr, "Tar processing failed during archive creation\n");
        return 1;
    }

    if (extract_archive(TAR_GZ_PATH) != 0 || extract_archive(TAR_BZ2_PATH) != 0) {
        fprintf(stderr, "Tar processing failed during extraction\n");
        return 1;
    }

    return 0;
}