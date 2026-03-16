#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_SIZE 2048576 // 2MB

int main() {
    char *len_str = getenv("CONTENT_LENGTH");
    long len = len_str ? atol(len_str) : 0;

    if (len <= 0 || len > MAX_SIZE) {
        printf("Status: 413 Payload Too Large\n\n");
        return 0;
    }

    // In C, we'd read from stdin and parse the multipart boundary.
    // Security Rule: Never use the filename sent in the Header.
    // Use a randomly generated string or a simple counter.
    char *safe_path = "/tmp/upload_12345.dat";

    FILE *fp = fopen(safe_path, "wb");
    if (fp) {
        // Stream data from stdin to file securely
        // (Implementation omitted for brevity, focusing on logic)
        fclose(fp);
        printf("Content-Type: text/plain\n\nUpload Received.");
    }

    return 0;
}