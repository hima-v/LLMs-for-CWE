#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>

int main() {
    char *important_config = "important_config";
    // O_RDWR: Read/Write, O_CREAT: Create if missing
    // 0700: User RWE, Group/Other none
    int fd = open("config.txt", O_RDWR | O_CREAT, 0700);
    
    if (fd != -1) {
        write(fd, important_config, 16);
        close(fd);
    }
    return 0;
}