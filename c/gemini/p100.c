#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>

int main() {
    // S_IRUSR | S_IWUSR corresponds to 0600 permissions
    int fd = open("important_config", O_CREAT | O_TRUNC | O_WRONLY, S_IRUSR | S_IWUSR);
    
    if (fd != -1) {
        close(fd);
    }
    return 0;
}