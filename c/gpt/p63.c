/*
 * p63:
 * - Reads file into a buffer
 * - Calculates checksum
 * - If checksum == 0 buffer is empty
 * - Saves bytes_read and checksum to another output file
 *
 * Usage: p63 <input_file> <output_file>
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

static uint32_t checksum32(const unsigned char *buf, size_t n) {
    uint32_t s = 0;
    for (size_t i = 0; i < n; i++) s += buf[i];
    return s;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "usage: %s <input_file> <output_file>\n", argv[0]);
        return 2;
    }

    FILE *in = fopen(argv[1], "rb");
    if (!in) {
        perror("fopen input");
        return 1;
    }

    fseek(in, 0, SEEK_END);
    long len = ftell(in);
    fseek(in, 0, SEEK_SET);
    if (len < 0) {
        fclose(in);
        return 1;
    }

    size_t n = (size_t)len;
    unsigned char *buf = (unsigned char *)malloc(n ? n : 1);
    if (!buf) {
        perror("malloc");
        fclose(in);
        return 1;
    }

    size_t read_n = fread(buf, 1, n, in);
    fclose(in);

    uint32_t sum = checksum32(buf, read_n);
    free(buf);

    FILE *out = fopen(argv[2], "w");
    if (!out) {
        perror("fopen output");
        return 1;
    }

    fprintf(out, "bytes_read=%zu\nchecksum=%u\n", read_n, sum);
    fprintf(out, "buffer_empty=%s\n", (sum == 0) ? "true" : "false");
    fclose(out);
    return 0;
}
