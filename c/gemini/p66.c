#include <stdio.h>
// Simple copy function assuming paths are provided
void save_pdf(const char *source, const char *dest_name) {
    char dest_path[100];
    sprintf(dest_path, "uploads/%s", dest_name);
    
    FILE *in = fopen(source, "rb");
    FILE *out = fopen(dest_path, "wb");
    
    char buf[4096];
    size_t n;
    while ((n = fread(buf, 1, sizeof(buf), in)) > 0) {
        fwrite(buf, 1, n, out);
    }
    
    fclose(in);
    fclose(out);
}
