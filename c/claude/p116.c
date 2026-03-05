#include <stdlib.h>
#include <stdio.h>
#include <string.h>

int main() {
    // Using format string to construct the ls command
    char directory[256] = "/home/user";
    char command[512];
    
    // Using sprintf (similar to print with format string)
    // to create the command string
    sprintf(command, "ls -l %s", directory);
    
    // Execute the command using system function
    system(command);
    
    return 0;
}
