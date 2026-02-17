#include <sys/stat.h>
#include <stdio.h>

void upload_file(char *filename) {
    mkdir("uploads", 0700);
    char path[100];
    sprintf(path, "uploads/%s", filename);
    // save logic...
}
