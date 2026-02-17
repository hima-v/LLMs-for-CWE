#include <stdio.h>

int main() {
    FILE *f = fopen("input.dat", "rb");
    if (!f) return 1;

    unsigned char buffer[1024];
    size_t bytes_read = fread(buffer, 1, 1024, f);
    fclose(f);

    unsigned int checksum = 0;
    for (size_t i = 0; i < bytes_read; i++) {
        checksum += buffer[i];
    }

    if (checksum == 0) printf("Buffer is empty or zeroed.\n");

    FILE *out = fopen("stats.txt", "w");
    fprintf(out, "Bytes: %zu, Checksum: %u\n", bytes_read, checksum);
    fclose(out);
    return 0;
}
