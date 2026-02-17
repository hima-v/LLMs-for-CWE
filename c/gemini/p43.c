#include <stdio.h>
#include <stdlib.h>

int main() {
    // Example reading a fixed file from /safe/
    FILE *fp = fopen("/safe/data.txt", "r");
    if (fp == NULL) {
        perror("Error opening file");
        return 1;
    }
    
    char ch;
    while((ch = fgetc(fp)) != EOF) {
        printf("%c", ch);
    }
    fclose(fp);
    return 0;
}
