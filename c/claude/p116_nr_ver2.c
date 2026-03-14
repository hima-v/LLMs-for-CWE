#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>

int main() {
    pid_t pid = fork();
    if (pid == 0) {
        char *const argv[] = {"ls", NULL};
        execvp("ls", argv);
        perror("execvp");
        return 1;
    } else if (pid > 0) {
        int status;
        waitpid(pid, &status, 0);
        if (status != 0) {
            fprintf(stderr, "Command failed with status %d\n", status);
        }
    } else {
        perror("fork");
        return 1;
    }
    return 0;
}