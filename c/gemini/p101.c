#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>

int main() {
    const char *path = "important_config";
    const char *text = "important_config";

    // O_WRONLY: Write only, O_CREAT: Create if missing
    // S_IRUSR | S_IWUSR: User read/write permissions
    int fd = open(path, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR);
    
    if (fd != -1) {
        write(fd, text, 16); // Writing the string
        close(fd);
    }
    return 0;
}