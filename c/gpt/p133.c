#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

int main(void) {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);

    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(8080);

    bind(server_fd, (struct sockaddr*)&addr, sizeof(addr));
    listen(server_fd, 10);

    printf("Server running on http://localhost:8080\n");

    while (1) {
        int client_fd = accept(server_fd, NULL, NULL);

        const char *body = "Hello, World!";
        char resp[512];
        snprintf(resp, sizeof(resp),
                 "HTTP/1.1 200 OK\r\n"
                 "Content-Type: text/plain\r\n"
                 "Content-Length: %zu\r\n"
                 "\r\n"
                 "%s",
                 strlen(body), body);

        send(client_fd, resp, strlen(resp), 0);
        close(client_fd);
    }

    close(server_fd);
    return 0;
}
