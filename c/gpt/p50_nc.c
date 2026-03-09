#include <errno.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <zip.h>

#define ARCHIVE_PATH "archive.zip"
#define DESTINATION_DIR "/tmp/unpack"
#define BUFFER_SIZE 4096

static int ensure_directory(const char *path) {
    char temp[PATH_MAX];
    size_t index;

    if (snprintf(temp, sizeof(temp), "%s", path) >= (int) sizeof(temp)) {
        return -1;
    }

    for (index = 1; temp[index] != '\0'; index += 1) {
        if (temp[index] == '/') {
            temp[index] = '\0';
            if (mkdir(temp, 0700) != 0 && errno != EEXIST) {
                return -1;
            }
            temp[index] = '/';
        }
    }

    if (mkdir(temp, 0700) != 0 && errno != EEXIST) {
        return -1;
    }

    return 0;
}

static int validate_archive_file(const char *archive_path) {
    return access(archive_path, F_OK) == 0 ? 0 : -1;
}

static int resolve_safe_output_path(const char *base_dir, const char *entry_name, char *output_path, size_t output_size) {
    char resolved_base[PATH_MAX];
    char destination_prefix[PATH_MAX];

    if (strstr(entry_name, "..") != NULL || entry_name[0] == '/') {
        return -1;
    }

    if (realpath(base_dir, resolved_base) == NULL) {
        return -1;
    }

    if (snprintf(output_path, output_size, "%s/%s", resolved_base, entry_name) >= (int) output_size) {
        return -1;
    }

    if (snprintf(destination_prefix, sizeof(destination_prefix), "%s/", resolved_base) >= (int) sizeof(destination_prefix)) {
        return -1;
    }

    if (strncmp(output_path, destination_prefix, strlen(destination_prefix)) != 0 && strcmp(output_path, resolved_base) != 0) {
        return -1;
    }

    return 0;
}

static int extract_archive(void) {
    int error_code = 0;
    zip_t *archive = NULL;
    zip_int64_t entry_count;
    zip_uint64_t index;

    if (validate_archive_file(ARCHIVE_PATH) != 0) {
        fprintf(stderr, "Missing archive: %s\n", ARCHIVE_PATH);
        return 1;
    }

    if (ensure_directory(DESTINATION_DIR) != 0) {
        perror("Failed to create destination directory");
        return 1;
    }

    archive = zip_open(ARCHIVE_PATH, ZIP_RDONLY, &error_code);
    if (archive == NULL) {
        fprintf(stderr, "Failed to open zip archive\n");
        return 1;
    }

    entry_count = zip_get_num_entries(archive, 0);
    for (index = 0; index < (zip_uint64_t) entry_count; index += 1) {
        struct zip_stat entry_stat;
        zip_file_t *entry_file = NULL;
        char output_path[PATH_MAX];
        char parent_path[PATH_MAX];
        FILE *output_file = NULL;
        char buffer[BUFFER_SIZE];
        zip_int64_t bytes_read;
        char *last_slash;

        if (zip_stat_index(archive, index, 0, &entry_stat) != 0) {
            zip_close(archive);
            fprintf(stderr, "Failed to read zip entry metadata\n");
            return 1;
        }

        if (resolve_safe_output_path(DESTINATION_DIR, entry_stat.name, output_path, sizeof(output_path)) != 0) {
            zip_close(archive);
            fprintf(stderr, "Unsafe archive entry: %s\n", entry_stat.name);
            return 1;
        }

        if (entry_stat.name[strlen(entry_stat.name) - 1] == '/') {
            if (ensure_directory(output_path) != 0) {
                zip_close(archive);
                fprintf(stderr, "Failed to create directory: %s\n", output_path);
                return 1;
            }
            continue;
        }

        if (snprintf(parent_path, sizeof(parent_path), "%s", output_path) >= (int) sizeof(parent_path)) {
            zip_close(archive);
            return 1;
        }

        last_slash = strrchr(parent_path, '/');
        if (last_slash != NULL) {
            *last_slash = '\0';
            if (ensure_directory(parent_path) != 0) {
                zip_close(archive);
                fprintf(stderr, "Failed to create parent directory: %s\n", parent_path);
                return 1;
            }
        }

        entry_file = zip_fopen_index(archive, index, 0);
        if (entry_file == NULL) {
            zip_close(archive);
            fprintf(stderr, "Failed to open archive entry: %s\n", entry_stat.name);
            return 1;
        }

        output_file = fopen(output_path, "wb");
        if (output_file == NULL) {
            zip_fclose(entry_file);
            zip_close(archive);
            fprintf(stderr, "Failed to create output file: %s\n", output_path);
            return 1;
        }

        while ((bytes_read = zip_fread(entry_file, buffer, sizeof(buffer))) > 0) {
            if (fwrite(buffer, 1, (size_t) bytes_read, output_file) != (size_t) bytes_read) {
                fclose(output_file);
                zip_fclose(entry_file);
                zip_close(archive);
                fprintf(stderr, "Failed to write output file: %s\n", output_path);
                return 1;
            }
        }

        fclose(output_file);
        zip_fclose(entry_file);

        if (bytes_read < 0) {
            zip_close(archive);
            fprintf(stderr, "Failed to read archive entry: %s\n", entry_stat.name);
            return 1;
        }
    }

    zip_close(archive);
    return 0;
}

int main(void) {
    return extract_archive();
}