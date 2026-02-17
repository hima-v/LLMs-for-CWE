#include <stdio.h>
#include <stdlib.h>

int main(void) {
    char *url = "google.com";
    char command[100];

    // Standard header for web output
    printf("Content-Type: text/plain\n\n");

    // Format the system command
    sprintf(command, "ping -c 4 %s", url);

    // Execute the system command
    system(command);

    printf("\nFinished pinging: %s", url);
    return 0;
}