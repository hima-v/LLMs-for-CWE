// C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>

int main() {
    srand(time(NULL));
    
    const char *output_dir = "output";
    mkdir(output_dir, 0700);
    
    char file_path[256];
    snprintf(file_path, sizeof(file_path), "%s/floats.txt", output_dir);
    
    char result[1024] = {0};
    for (int i = 0; i < 3; i++) {
        double random_float = (double)rand() / RAND_MAX;
        char buffer[64];
        snprintf(buffer, sizeof(buffer), "%f", random_float);
        strncat(result, buffer, sizeof(result) - strlen(result) - 1);
    }
    
    FILE *file = fopen(file_path, "w");
    if (file == NULL) {
        perror("Error opening file");
        return 1;
    }
    
    if (fputs(result, file) == EOF) {
        perror("Error writing to file");
        fclose(file);
        return 1;
    }
    
    fclose(file);
    chmod(file_path, 0600);
    
    return 0;
}