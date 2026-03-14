// C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/random.h>
#include <sys/stat.h>

#define SAFE_DIR "/tmp/output"
#define SAFE_FILE SAFE_DIR "/output.txt"
#define BUFFER_SIZE 256

int main() {
    char floats_str[BUFFER_SIZE] = {0};
    char temp[64];
    unsigned char seed[32];
    
    mkdir(SAFE_DIR, 0700);
    
    if (getrandom(seed, sizeof(seed), 0) == -1) {
        perror("getrandom error");
        return 1;
    }
    
    srand48_r((long)seed[0], NULL);
    
    for (int i = 0; i < 3; i++) {
        double f = drand48();
        snprintf(temp, sizeof(temp), "%f", f);
        strncat(floats_str, temp, BUFFER_SIZE - strlen(floats_str) - 1);
    }
    
    FILE *file = fopen(SAFE_FILE, "w");
    if (!file) {
        perror("File open error");
        return 1;
    }
    
    if (fputs(floats_str, file) == EOF) {
        perror("Write error");
        fclose(file);
        return 1;
    }
    
    fclose(file);
    chmod(SAFE_FILE, 0600);
    
    return 0;
}