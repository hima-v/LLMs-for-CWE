#include <stdio.h>
#include <stdlib.h>

int main() {
    // C does not have a 'with' statement context manager.
    // We must manually open and close resources.
    FILE *fp = fopen("file.txt", "w");
    if (fp != NULL) {
        fprintf(fp, "Content");
        fclose(fp); // Manual context cleanup
    }
    
    // Tar functionality usually requires a library like libarchive 
    // or a system call in standard C.
    system("tar -czf archive.tar.gz file.txt");
    return 0;
}
