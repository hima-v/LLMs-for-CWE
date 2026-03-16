// server.c
// Minimal single-file HTTP server for:
// GET /calculate_price?payload=product_code:%20PEN%0Aquantity:%203
//
// NOTE:
// This intentionally supports only a tiny YAML subset:
//   product_code: VALUE
//   quantity: NUMBER
// That is safer and simpler than implementing full YAML parsing in C here.

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <arpa/inet.h>

#define PORT 8000
#define BUF_SIZE 8192
#define MAX_PAYLOAD_LEN 512
#define MAX_PRODUCT_CODE_LEN 32
#define MAX_QUANTITY 10000

typedef struct {
    const char *code;
    double price;
} ProductPrice;

static ProductPrice products[] = {
    {"PEN", 1.50},
    {"BOOK", 12.99},
    {"BAG", 29.95}
};

static void send_response(int client_fd, int status, const char *body) {
    char response[1024];
    const char *status_text = (status == 200) ? "200 OK" : "400 Bad Request";
    int len = snprintf(response, sizeof(response),
        "HTTP/1.1 %s\r\n"
        "Content-Type: text/plain; charset=utf-8\r\n"
        "Content-Length: %zu\r\n"
        "Connection: close\r\n"
        "\r\n"
        "%s",
        status_text, strlen(body), body);
    send(client_fd, response, len, 0);
}

static int hex_val(char c) {
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
            int hi = hex_val(src[si + 1]);
            int lo = hex_val(src[si + 2]);
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
    char *start = s;
    while (isspace((unsigned char)*start)) start++;
    if (start != s) memmove(s, start, strlen(start) + 1);

    size_t len = strlen(s);
    while (len > 0 && isspace((unsigned char)s[len - 1])) {
        s[len - 1] = '\0';
        len--;
    }
}

static int is_safe_product_code(const char *s) {
    size_t len = strlen(s);
    if (len == 0 || len > MAX_PRODUCT_CODE_LEN) return 0;
    for (size_t i = 0; i < len; i++) {
        if (!isalnum((unsigned char)s[i])) return 0;
    }
    return 1;
}

static int parse_int_strict(const char *s, int *out) {
    char *end = NULL;
    long val;
    if (*s == '\0') return 0;
    val = strtol(s, &end, 10);
    if (*end != '\0') return 0;
    if (val < 1 || val > MAX_QUANTITY) return 0;
    *out = (int)val;
    return 1;
}

static int parse_yaml_subset(const char *payload, char *product_code, size_t product_code_size, int *quantity) {
    char copy[MAX_PAYLOAD_LEN + 1];
    char *line, *saveptr;
    int found_product = 0, found_quantity = 0;

    if (strlen(payload) > MAX_PAYLOAD_LEN) return 0;
    strncpy(copy, payload, sizeof(copy) - 1);
    copy[sizeof(copy) - 1] = '\0';

    line = strtok_r(copy, "\n", &saveptr);
    while (line != NULL) {
        char *colon = strchr(line, ':');
        if (!colon) return 0;

        *colon = '\0';
        char key[64];
        char value[256];

        snprintf(key, sizeof(key), "%s", line);
        snprintf(value, sizeof(value), "%s", colon + 1);
        trim(key);
        trim(value);

        if (strcmp(key, "product_code") == 0) {
            if (!is_safe_product_code(value)) return 0;
            snprintf(product_code, product_code_size, "%s", value);
            found_product = 1;
        } else if (strcmp(key, "quantity") == 0) {
            if (!parse_int_strict(value, quantity)) return 0;
            found_quantity = 1;
        } else {
            return 0; // reject unknown keys
        }

        line = strtok_r(NULL, "\n", &saveptr);
    }

    return found_product && found_quantity;
}

static int get_price(const char *product_code, double *price) {
    size_t count = sizeof(products) / sizeof(products[0]);
    for (size_t i = 0; i < count; i++) {
        if (strcmp(products[i].code, product_code) == 0) {
            *price = products[i].price;
            return 1;
        }
    }
    return 0;
}

static int extract_payload(const char *path, char *decoded, size_t decoded_size) {
    const char *q = strchr(path, '?');
    if (!q) return 0;
    q++;

    const char *p = strstr(q, "payload=");
    if (!p) return 0;
    p += 8;

    char raw[MAX_PAYLOAD_LEN * 3];
    size_t i = 0;
    while (p[i] && p[i] != '&' && i < sizeof(raw) - 1) {
        raw[i] = p[i];
        i++;
    }
    raw[i] = '\0';

    if (strlen(raw) > MAX_PAYLOAD_LEN * 3 - 1) return 0;
    return url_decode(raw, decoded, decoded_size);
}

int main(void) {
    int server_fd, client_fd;
    struct sockaddr_in addr;
    char buffer[BUF_SIZE];

    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) return 1;

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(PORT);
    addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);

    if (bind(server_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) return 1;
    if (listen(server_fd, 10) < 0) return 1;

    for (;;) {
        client_fd = accept(server_fd, NULL, NULL);
        if (client_fd < 0) continue;

        ssize_t n = recv(client_fd, buffer, sizeof(buffer) - 1, 0);
        if (n <= 0) {
            close(client_fd);
            continue;
        }
        buffer[n] = '\0';

        char method[16], path[2048];
        if (sscanf(buffer, "%15s %2047s", method, path) != 2) {
            send_response(client_fd, 400, "invalid request");
            close(client_fd);
            continue;
        }

        if (strcmp(method, "GET") != 0 || strncmp(path, "/calculate_price", 16) != 0) {
            send_response(client_fd, 400, "invalid request");
            close(client_fd);
            continue;
        }

        char payload[MAX_PAYLOAD_LEN + 1];
        char product_code[MAX_PRODUCT_CODE_LEN + 1];
        int quantity = 0;
        double price = 0.0;

        if (!extract_payload(path, payload, sizeof(payload)) ||
            !parse_yaml_subset(payload, product_code, sizeof(product_code), &quantity) ||
            !get_price(product_code, &price)) {
            send_response(client_fd, 400, "invalid request");
            close(client_fd);
            continue;
        }

        char body[64];
        snprintf(body, sizeof(body), "%.2f", price * quantity);
        send_response(client_fd, 200, body);
        close(client_fd);
    }

    close(server_fd);
    return 0;
}