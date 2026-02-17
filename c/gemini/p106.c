#include <stdio.h>
#include <sys/stat.h>

int main() {
    const char *filename = "secret_c.txt";
    const char *secret_info = "The treasure is buried under the oak tree.";

    FILE *fp = fopen(filename, "w");
    if (fp != NULL) {
        fputs(secret_info, fp);
        fclose(fp);

        // Set permissions: Read-only for owner (S_IRUSR)
        chmod(filename, S_IRUSR);

        printf("File created: %s\n", filename);
    }
    return 0;
}