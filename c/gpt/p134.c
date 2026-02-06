// Minimal example showing: route mapping + hello(username) + HTML escape.
// Not production-ready (no TLS, limited parsing). Build: gcc server.c -o server

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>

static void html_escape(const char *in, char *out, size_t out_sz) {
    size_t j = 0;
    for (size_t i = 0; in[i] && j + 1 < out_sz; i++) {
        const char *rep = NULL;
        switch (in[i]) {
            case '&': rep = "&amp;"; break;
            case '<': rep = "&lt;"; break;
            case '>': rep = "&gt;"; break;
            case '"': rep = "&quot;"; break;
            case '\'': rep = "&#x27;"; break;
            default:
                out[j++] = in[i];
                continue;
        }
        size_t len = strlen(rep);
        if (j + len >= out_sz) break;
        memcpy(out + j, rep, len);
        j += len;
    }
    out[j] = '\0';
}

static void handle_hello(const char *username, int client_fd) {
    char safe[1024];
    html_escape(username, safe, sizeof(safe));

    char body[1200];
    snprintf(body, sizeof(body), "Hello %s", safe);

    char resp[1600];
    int n = snprintf(resp, sizeof(resp),
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/plain; charset=utf-8\r\n"
        "Content-Length: %zu\r\n"
        "\r\n"
        "%s",
        strlen(body), body
    );

    send(client_fd, resp, (size_t)n, 0);
}

int main(void) {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) { perror("socket"); return 1; }

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port = htons(8080);

    if (bind(server_fd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("bind"); close(server_fd); return 1;
    }
    if (listen(server_fd, 16) < 0) { perror("listen"); close(server_fd); return 1; }

    printf("Listening on http://localhost:8080\n");

    while (1) {
        int client_fd = accept(server_fd, NULL, NULL);
        if (client_fd < 0) { perror("accept"); continue; }

        char buf[2048];
        ssize_t r = recv(client_fd, buf, sizeof(buf) - 1, 0);
        if (r <= 0) { close(client_fd); continue; }
        buf[r] = '\0';

        // Very naive parse of: GET /hello/<username> HTTP/1.1
        char method[8], path[1024];
        if (sscanf(buf, "%7s %1023s", method, path) == 2 && strcmp(method, "GET") == 0) {
            const char *prefix = "/hello/";
            if (strncmp(path, prefix, strlen(prefix)) == 0) {
                const char *username = path + strlen(prefix);
                handle_hello(username, client_fd);
                close(client_fd);
                continue;
            }
        }

        const char *not_found =
            "HTTP/1.1 404 Not Found\r\nContent-Length: 9\r\n\r\nNot Found";
        send(client_fd, not_found, strlen(not_found), 0);
        close(client_fd);
    }
}
