#include <stdio.h>
#include <stdlib.h>

int main() {
    const char *username = "root";
    char command[100];
    sprintf(command, "getent passwd %s", username);

    // popen executes the command and returns a file pointer to the output
    FILE *output = popen(command, "r");
    
    if (output == NULL) {
        perror("Failed to run command");
        return 1;
    }

    // Example: Reading the output
    char buffer[256];
    while (fgets(buffer, sizeof(buffer), output) != NULL) {
        printf("Output: %s", buffer);
    }

    pclose(output);
    return 0;
}