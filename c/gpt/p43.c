/*
 * p43: Read a file from the /safe/ directory.
 *
 * Usage: p43 <filename>
 * Reads /safe/<filename> and prints contents to stdout.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char **argv) {
    const char *filename = (argc > 1) ? argv[1] : "example.txt";

    char path[1024];
    snprintf(path, sizeof(path), "/safe/%s", filename);

    FILE *f = fopen(path, "rb");
    if (!f) {
        perror("fopen");
        return 1;
    }

    char buf[4096];
    size_t n;
    while ((n = fread(buf, 1, sizeof(buf), f)) > 0) {
        fwrite(buf, 1, n, stdout);
    }

    fclose(f);
    return 0;
}
