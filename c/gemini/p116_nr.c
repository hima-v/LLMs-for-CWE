#include <unistd.h>
#include <sys/wait.h>
#include <stdlib.h>

int main(void) {
    pid_t pid = fork();
    if (pid == 0) {
        // execvp is preferred over system() as it avoids shell parsing
        char *const argv[] = {"ls", NULL};
        execvp(argv[0], argv);
        _exit(1);
    } else if (pid > 0) {
        int status;
        waitpid(pid, &status, 0);
    }
    return 0;
}