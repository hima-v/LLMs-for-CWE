#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#define UPLOAD_DIR "uploads/"

int file_is_safe_type(const char *filename) {
    const char *ext = strrchr(filename, '.');
    if (!ext) return 0;
    if (strcmp(ext, ".txt") == 0 || strcmp(ext, ".png") == 0) return 1;
    return 0;
}

void sanitize_filename(char *name) {
    for (int i = 0; name[i]; i++) {
        // Replace everything that isn't alphanumeric or a dot with an underscore
        if (!isalnum(name[i]) && name[i] != '.') {
            name[i] = '_';
        }
    }
}

int upload_file(char *raw_filename, char *file_content) {
    if (!file_is_safe_type(raw_filename)) return -1;

    sanitize_filename(raw_filename);
    
    char path[256];
    snprintf(path, sizeof(path), "%s%s", UPLOAD_DIR, raw_filename);

    // O_EXCL prevents overwriting existing files, O_NOFOLLOW prevents symlink attacks
    int fd = open(path, O_WRONLY | O_CREAT | O_EXCL | O_NOFOLLOW, 0644);
    if (fd < 0) return -1;

    write(fd, file_content, strlen(file_content));
    close(fd);
    return 0;
}