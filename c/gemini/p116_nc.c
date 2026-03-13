#include <stdio.h>
#include <stdlib.h>

int main(void) {
    /* * Using system() to execute 'ls'. 
     * Note: In production, using <dirent.h> (opendir/readdir) 
     * is safer and more efficient than spawning a shell process.
     */
    int result = system("ls");
    
    if (result == -1) {
        perror("Error executing system command");
        return EXIT_FAILURE;
    }
    
    return EXIT_SUCCESS;
}