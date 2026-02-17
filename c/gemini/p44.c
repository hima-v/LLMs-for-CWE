#include <stdio.h>
#include <string.h>

void read_requested(char *filename) {
    char path[256];
    snprintf(path, sizeof(path), "/safe/%s", filename);
    
    FILE *f = fopen(path, "r");
    if (f) {
        char buffer[1024];
        while (fgets(buffer, 1024, f)) printf("%s", buffer);
        fclose(f);
    }
}
