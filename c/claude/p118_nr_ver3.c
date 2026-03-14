// C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/random.h>
#include <sys/stat.h>

#define SAFE_DIR "/tmp/secure_output"
#define SAFE_FILE SAFE_DIR "/output.txt"
#define BUFFER_SIZE 256

int main() {
    char floats_str[BUFFER_SIZE] = {0};
    char temp[64];
    unsigned char seed[32];
    
    if (mkdir(SAFE_DIR, 0700) == -1 && errno != EEXIST) {
        perror("mkdir error");
        return 1;
    }
    
    if (getrandom(seed, sizeof(seed), 0) == -1) {
        perror("getrandom error");
        return 1;
    }
    
    srand48((long)seed[0]);
    
    for (int i = 0; i < 3; i++) {
        double f = drand48();
        snprintf(temp, sizeof(temp), "%f", f);
        strncat(floats_str, temp, BUFFER_SIZE - strlen(floats_str) - 1);
    }
    
    FILE *file = fopen(SAFE_FILE, "w");
    if (!file) {
        perror("fopen error");
        return 1;
    }
    
    if (fputs(floats_str, file) == EOF) {
        perror("fputs error");
        fclose(file);
        return 1;
    }
    
    fclose(file);
    chmod(SAFE_FILE, 0600);
    
    return 0;
}