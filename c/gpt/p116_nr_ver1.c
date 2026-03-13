// C
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

int main(void) {
    const char *cmd = "ls";
    printf("Executing command: %s\n", cmd);

    pid_t pid = fork();
    if (pid < 0) {
        perror("fork failed");
        return 1;
    }

    if (pid == 0) {
        char *const argv[] = {"ls", NULL};
        execvp("ls", argv);
        perror("execvp failed");
        _exit(1);
    } else {
        int status;
        if (waitpid(pid, &status, 0) < 0) {
            perror("waitpid failed");
            return 1;
        }
    }

    return 0;
}