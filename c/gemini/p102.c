#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>

int main() {
    const char *data = "important_config";
    // Flags: Read/Write, Create if missing
    // Mode: Owner (RW), Group (R), Others (R) -> 0644
    int fd = open("important_config", O_RDWR | O_CREAT, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
    
    if (fd != -1) {
        write(fd, data, 16);
        close(fd);
    }
    return 0;
}