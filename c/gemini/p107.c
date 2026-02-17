#include <stdio.h>
#include <sys/stat.h>

int main() {
    char *filename = "secret.txt";
    FILE *fp = fopen(filename, "w");
    fprintf(fp, "U2FsdGVkX1/R+WzJcxgvX/Iw==");
    fclose(fp);

    // Change permissions using decimal 128
    chmod(filename, 128);

    struct stat st;
    stat(filename, &st);
    
    // %o prints the number in octal format
    printf("The permissions of the file are: 0o%o\n", st.st_mode & 0777);
    return 0;
}