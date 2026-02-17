#include <stdio.h>
#include <sys/stat.h>

int main() {
    const char *filename = "secret.txt";
    const char *content = "U2FsdGVkX1/R+WzJcxgvX/Iw==";

    FILE *fp = fopen(filename, "w");
    if (fp == NULL) return 1;

    fputs(content, fp);
    fclose(fp);

    // S_IRUSR (read) | S_IWUSR (write) for owner
    if (chmod(filename, S_IRUSR | S_IWUSR) == 0) {
        printf("File created and permissions set to 600.\n");
    }

    return 0;
}