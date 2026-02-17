/*
 * p44: Read a requested file from the /safe/ directory.
 *
 * Usage: p44 <filename>
 * Reads /safe/<filename> and prints it.
 */

#include <stdio.h>

int main(int argc, char **argv) {
    const char *requested = (argc > 1) ? argv[1] : "example.txt";
    char path[1024];
    snprintf(path, sizeof(path), "/safe/%s", requested);

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
