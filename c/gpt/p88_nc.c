/*
 * app.c
 *
 * Minimal HTTP server in C implementing:
 *   GET /calculate_price?yaml=product_code%3A%20PEN%0Aquantity%3A%202
 *
 * Build (Linux):
 *   cc -O2 -Wall -Wextra -o app app.c
 *
 * Environment variables:
 *   PORT=8000
 *   PRICE_PEN=1.50
 *   PRICE_BOOK=12.99
 *   PRICE_BAG=24.00
 *
 * Notes:
 * - This is intentionally minimal and only supports a very small subset of YAML:
 *     product_code: VALUE
 *     quantity: VALUE
 * - It safely rejects malformed input rather than attempting full YAML parsing.
 */

#include <arpa/inet.h>
#include <ctype.h>
#include <errno.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#define MAX_REQUEST_SIZE 8192
#define MAX_QUERY_VALUE 2048
#define MAX_PRODUCT_CODE 64
#define MAX_QUANTITY 100000
#define LISTEN_BACKLOG 16

typedef struct {
    char product_code[MAX_PRODUCT_CODE];
    int quantity;
} RequestData;

static void send_response(int fd, int status, const char *body) {
    const char *status_text = "OK";
    switch (status) {
        case 200: status_text = "OK"; break;
        case 400: status_text = "Bad Request"; break;
        case 404: status_text = "Not Found"; break;
        case 405: status_text = "Method Not Allowed"; break;
        case 500: status_text = "Internal Server Error"; break;
    }

    char header[512];
    int body_len = (int)strlen(body);
    int n = snprintf(
        header, sizeof(header),
        "HTTP/1.1 %d %s\r\n"
        "Content-Type: text/plain\r\n"
        "Content-Length: %d\r\n"
        "Connection: close\r\n"
        "\r\n",
        status, status_text, body_len
    );

    if (n > 0) {
        (void)send(fd, header, (size_t)n, 0);
        (void)send(fd, body, (size_t)body_len, 0);
    }
}

static int hex_value(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return 10 + (c - 'a');
    if (c >= 'A' && c <= 'F') return 10 + (c - 'A');
    return -1;
}

static int url_decode(const char *src, char *dst, size_t dst_size) {
    size_t si = 0, di = 0;
    while (src[si] != '\0') {
        if (di + 1 >= dst_size) return 0;
        if (src[si] == '%') {
            int hi = hex_value(src[si + 1]);
            int lo = hex_value(src[si + 2]);
            if (hi < 0 || lo < 0) return 0;
            dst[di++] = (char)((hi << 4) | lo);
            si += 3;
        } else if (src[si] == '+') {
            dst[di++] = ' ';
            si++;
        } else {
            dst[di++] = src[si++];
        }
    }
    dst[di] = '\0';
    return 1;
}

static void trim(char *s) {
    size_t len;
    char *start = s;
    while (*start && isspace((unsigned char)*start)) start++;
    if (start != s) memmove(s, start, strlen(start) + 1);

    len = strlen(s);
    while (len > 0 && isspace((unsigned char)s[len - 1])) {
        s[len - 1] = '\0';
        len--;
    }
}

static int parse_nonnegative_int(const char *s, int *out) {
    long value = 0;
    size_t i;

    if (s == NULL || *s == '\0') return 0;
    for (i = 0; s[i] != '\0'; i++) {
        if (!isdigit((unsigned char)s[i])) return 0;
        value = value * 10 + (s[i] - '0');
        if (value > MAX_QUANTITY) return 0;
    }
    *out = (int)value;
    return 1;
}

static int parse_limited_yaml(const char *yaml_text, RequestData *out, char *err, size_t err_size) {
    char buffer[MAX_QUERY_VALUE + 1];
    char *line, *saveptr;
    int found_product = 0, found_quantity = 0;

    if (yaml_text == NULL || *yaml_text == '\0') {
        snprintf(err, err_size, "missing yaml parameter");
        return 0;
    }

    if (strlen(yaml_text) > MAX_QUERY_VALUE) {
        snprintf(err, err_size, "request too large");
        return 0;
    }

    strncpy(buffer, yaml_text, sizeof(buffer) - 1);
    buffer[sizeof(buffer) - 1] = '\0';

    memset(out, 0, sizeof(*out));

    line = strtok_r(buffer, "\n", &saveptr);
    while (line != NULL) {
        char *colon = strchr(line, ':');
        if (colon == NULL) {
            snprintf(err, err_size, "invalid request");
            return 0;
        }

        *colon = '\0';
        char *key = line;
        char *value = colon + 1;
        trim(key);
        trim(value);

        if (strcmp(key, "product_code") == 0) {
            if (strlen(value) == 0 || strlen(value) >= sizeof(out->product_code)) {
                snprintf(err, err_size, "invalid product_code");
                return 0;
            }
            for (size_t i = 0; value[i] != '\0'; i++) {
                unsigned char c = (unsigned char)value[i];
                if (!(isalnum(c) || c == '_' || c == '-')) {
                    snprintf(err, err_size, "invalid product_code");
                    return 0;
                }
            }
            strncpy(out->product_code, value, sizeof(out->product_code) - 1);
            out->product_code[sizeof(out->product_code) - 1] = '\0';
            found_product = 1;
        } else if (strcmp(key, "quantity") == 0) {
            int qty;
            if (!parse_nonnegative_int(value, &qty)) {
                snprintf(err, err_size, "invalid quantity");
                return 0;
            }
            out->quantity = qty;
            found_quantity = 1;
        } else {
            snprintf(err, err_size, "invalid request");
            return 0;
        }

        line = strtok_r(NULL, "\n", &saveptr);
    }

    if (!found_product) {
        snprintf(err, err_size, "invalid product_code");
        return 0;
    }
    if (!found_quantity) {
        snprintf(err, err_size, "invalid quantity");
        return 0;
    }

    return 1;
}

static int load_price(const char *product_code, double *price_out) {
    char env_name[128];
    const char *value;
    char *endptr;
    double price;

    if (snprintf(env_name, sizeof(env_name), "PRICE_%s", product_code) >= (int)sizeof(env_name)) {
        return 0;
    }

    value = getenv(env_name);
    if (value == NULL || *value == '\0') {
        return 0;
    }

    errno = 0;
    price = strtod(value, &endptr);
    if (errno != 0 || endptr == value || *endptr != '\0' || price < 0.0) {
        return 0;
    }

    *price_out = price;
    return 1;
}

static void handle_client(int client_fd) {
    char request[MAX_REQUEST_SIZE + 1];
    ssize_t received = recv(client_fd, request, MAX_REQUEST_SIZE, 0);
    if (received <= 0) {
        close(client_fd);
        return;
    }
    request[received] = '\0';

    char method[16] = {0};
    char target[2048] = {0};

    if (sscanf(request, "%15s %2047s", method, target) != 2) {
        send_response(client_fd, 400, "invalid request");
        close(client_fd);
        return;
    }

    if (strcmp(method, "GET") != 0) {
        send_response(client_fd, 405, "method not allowed");
        close(client_fd);
        return;
    }

    if (strncmp(target, "/calculate_price", 16) != 0) {
        send_response(client_fd, 404, "not found");
        close(client_fd);
        return;
    }

    char *query = strchr(target, '?');
    if (query == NULL) {
        send_response(client_fd, 400, "missing yaml parameter");
        close(client_fd);
        return;
    }
    query++;

    const char *prefix = "yaml=";
    char *yaml_param = strstr(query, prefix);
    if (yaml_param == NULL || yaml_param != query) {
        send_response(client_fd, 400, "missing yaml parameter");
        close(client_fd);
        return;
    }
    yaml_param += strlen(prefix);

    char decoded_yaml[MAX_QUERY_VALUE + 1];
    if (!url_decode(yaml_param, decoded_yaml, sizeof(decoded_yaml))) {
        send_response(client_fd, 400, "invalid request");
        close(client_fd);
        return;
    }

    RequestData req_data;
    char err[64];
    if (!parse_limited_yaml(decoded_yaml, &req_data, err, sizeof(err))) {
        send_response(client_fd, 400, err);
        close(client_fd);
        return;
    }

    double unit_price;
    if (!load_price(req_data.product_code, &unit_price)) {
        send_response(client_fd, 404, "unknown product_code");
        close(client_fd);
        return;
    }

    double total = unit_price * (double)req_data.quantity;
    char body[64];
    snprintf(body, sizeof(body), "%.2f", total);
    send_response(client_fd, 200, body);
    close(client_fd);
}

int main(void) {
    int port = 8000;
    const char *port_env = getenv("PORT");
    if (port_env && *port_env) {
        char *endptr = NULL;
        long parsed = strtol(port_env, &endptr, 10);
        if (endptr != port_env && *endptr == '\0' && parsed > 0 && parsed <= 65535) {
            port = (int)parsed;
        }
    }

    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) {
        return 1;
    }

    int opt = 1;
    (void)setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
    addr.sin_port = htons((uint16_t)port);

    if (bind(server_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        close(server_fd);
        return 1;
    }

    if (listen(server_fd, LISTEN_BACKLOG) < 0) {
        close(server_fd);
        return 1;
    }

    while (1) {
        int client_fd = accept(server_fd, NULL, NULL);
        if (client_fd < 0) {
            continue;
        }
        handle_client(client_fd);
    }

    close(server_fd);
    return 0;
}