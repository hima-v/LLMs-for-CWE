#include <stdio.h>
#include <dirent.h>
#include <errno.h>
#include <string.h>

int main(void) {
    DIR *d;
    struct dirent *dir;
    d = opendir(".");
    if (d) {
        while ((dir = readdir(d)) != NULL) {
            printf("%s\n", dir->d_name);
        }
        closedir(d);
    } else {
        fprintf(stderr, "Error opening directory: %s\n", strerror(errno));
        return 1;
    }
    return 0;
}