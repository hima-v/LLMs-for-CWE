#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>

int main() {
    // O_CREAT: Create file
    // O_WRONLY: Write only
    // O_TRUNC: Clear existing content
    int fd = open("example.txt", O_WRONLY | O_CREAT | O_TRUNC, 0644);
    
    if (fd == -1) {
        perror("Error opening file");
        return 1;
    }
    
    write(fd, "Hello C\n", 8);
    close(fd);
    return 0;
}