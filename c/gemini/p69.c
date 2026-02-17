// Pseudo-code for logic (C requires external lib for base64 usually)
#include <stdio.h>
#include <stdlib.h>

void upload_image(char *filename) {
    FILE *f = fopen(filename, "rb");
    // ... Read bytes ...
    // ... Base64 Encode bytes to string ...
    FILE *out = fopen("output.txt", "w");
    fprintf(out, "%s", base64_string);
    fclose(f);
    fclose(out);
}
