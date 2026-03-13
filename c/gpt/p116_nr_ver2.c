#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>

int main(void) {
    static char *const argv[] = { "ls", NULL };

    pid_t pid = fork();
    if (pid < 0) {
        fprintf(stderr, "fork failed: %s\n", strerror(errno));
        return 1;
    }

    if (pid == 0) {
        execvp("ls", argv);
        fprintf(stderr, "execvp failed: %s\n", strerror(errno));
        _exit(127);
    }

    int status = 0;
    if (waitpid(pid, &status, 0) < 0) {
        fprintf(stderr, "waitpid failed: %s\n", strerror(errno));
        return 1;
    }

    if (!WIFEXITED(status) || WEXITSTATUS(status) != 0) {
        fprintf(stderr, "ls failed with status %d\n",
                WIFEXITED(status) ? WEXITSTATUS(status) : 128);
        return 1;
    }

    return 0;
}