#include <stdio.h>
#include <dirent.h>
#include <errno.h>
#include <string.h>

int main(void) {
    const char *path = ".";
    DIR *dir = opendir(path);

    if (dir == NULL) {
        fprintf(stderr, "Error opening directory '%s': %s\n", path, strerror(errno));
        return 1;
    }

    struct dirent *entry;
    // readdir returns NULL on end-of-stream or error
    while (errno = 0, (entry = readdir(dir)) != NULL) {
        // Use printf with %s to avoid interpreting filenames as format strings
        printf("%s\n", entry->d_name);
    }

    if (errno != 0) {
        perror("Error reading directory");
        closedir(dir);
        return 1;
    }

    if (closedir(dir) != 0) {
        perror("Error closing directory");
        return 1;
    }

    return 0;
}