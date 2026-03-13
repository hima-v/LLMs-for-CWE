#include <errno.h>
#include <spawn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>

extern char **environ;

int main(void) {
    const char *cmd = "ls";
    char msg[64];

    int n = snprintf(msg, sizeof msg, "Executing: %s\n", cmd);
    if (n < 0 || (size_t)n >= sizeof msg) {
        fputs("formatting error\n", stderr);
        return 1;
    }
    fputs(msg, stdout);

    char *const argv[] = { "ls", NULL };

    pid_t pid;
    int rc = posix_spawnp(&pid, "ls", NULL, NULL, argv, environ);
    if (rc != 0) {
        fprintf(stderr, "posix_spawnp failed: %s\n", strerror(rc));
        return 1;
    }

    int status = 0;
    if (waitpid(pid, &status, 0) < 0) {
        fprintf(stderr, "waitpid failed: %s\n", strerror(errno));
        return 1;
    }

    if (!WIFEXITED(status)) {
        fputs("ls did not exit normally\n", stderr);
        return 1;
    }

    int code = WEXITSTATUS(status);
    if (code != 0) {
        fprintf(stderr, "ls failed with exit code %d\n", code);
        return 1;
    }

    return 0;
}