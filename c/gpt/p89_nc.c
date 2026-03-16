#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

#define PORT 5000
#define BUFFER_SIZE 8192
#define MIN_QTY 1
#define MAX_QTY 1000

typedef struct {
    const char *code;
    double price;
} Product;

static const Product PRODUCTS[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 25.00},
    {"P400", 3.75}
};

static const size_t PRODUCT_COUNT = sizeof(PRODUCTS) / sizeof(PRODUCTS[0]);

int is_valid_product_code(const char *code) {
    size_t len = strlen(code);
    if (len < 2 || len > 10) {
        return 0;
    }

    for (size_t i = 0; i < len; i++) {
        if (!isalnum((unsigned char)code[i]) || islower((unsigned char)code[i])) {
            return 0;
        }
    }

    return 1;
}

int get_price(const char *product_code, double *price_out) {
    if (product_code == NULL || price_out == NULL) {
        return 0;
    }

    if (!is_valid_product_code(product_code)) {
        return 0;
    }

    for (size_t i = 0; i < PRODUCT_COUNT; i++) {
        if (strcmp(PRODUCTS[i].code, product_code) == 0) {
            *price_out = PRODUCTS[i].price;
            return 1;
        }
    }

    return 0;
}

int parse_quantity(const char *raw, int *qty_out) {
    char *endptr = NULL;
    long value;

    if (raw == NULL || qty_out == NULL || *raw == '\0') {
        return 0;
    }

    for (const char *p = raw; *p; p++) {
        if (!isdigit((unsigned char)*p)) {
            return 0;
        }
    }

    value = strtol(raw, &endptr, 10);

    if (*endptr != '\0') {
        return 0;
    }

    if (value < MIN_QTY || value > MAX_QTY) {
        return 0;
    }

    *qty_out = (int)value;
    return 1;
}

void url_decode(char *dst, const char *src, size_t dst_size) {
    size_t di = 0;

    while (*src && di + 1 < dst_size) {
        if (*src == '%' &&
            isxdigit((unsigned char)src[1]) &&
            isxdigit((unsigned char)src[2])) {
            char hex[3] = {src[1], src[2], '\0'};
            dst[di++] = (char)strtol(hex, NULL, 16);
            src += 3;
        } else if (*src == '+') {
            dst[di++] = ' ';
            src++;
        } else {
            dst[di++] = *src++;
        }
    }

    dst[di] = '\0';
}

void get_query_param(const char *query, const char *key, char *out, size_t out_size) {
    char query_copy[2048];
    char *token;
    out[0] = '\0';

    strncpy(query_copy, query, sizeof(query_copy) - 1);
    query_copy[sizeof(query_copy) - 1] = '\0';

    token = strtok(query_copy, "&");
    while (token != NULL) {
        char *equals = strchr(token, '=');
        if (equals != NULL) {
            *equals = '\0';
            if (strcmp(token, key) == 0) {
                url_decode(out, equals + 1, out_size);
                return;
            }
        }
        token = strtok(NULL, "&");
    }
}

void send_response(int client_fd, int status_code, const char *status_text, const char *json_body) {
    char response[4096];
    int body_len = (int)strlen(json_body);

    snprintf(
        response,
        sizeof(response),
        "HTTP/1.1 %d %s\r\n"
        "Content-Type: application/json\r\n"
        "Content-Length: %d\r\n"
        "Connection: close\r\n"
        "\r\n"
        "%s",
        status_code,
        status_text,
        body_len,
        json_body
    );

    send(client_fd, response, strlen(response), 0);
}

int main(void) {
    int server_fd, client_fd;
    struct sockaddr_in address;
    int opt = 1;
    socklen_t addrlen = sizeof(address);
    char buffer[BUFFER_SIZE];

    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) {
        return 1;
    }

    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    address.sin_family = AF_INET;
    address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
    address.sin_port = htons(PORT);

    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) {
        close(server_fd);
        return 1;
    }

    if (listen(server_fd, 10) < 0) {
        close(server_fd);
        return 1;
    }

    printf("Server running on http://127.0.0.1:%d\n", PORT);

    while (1) {
        client_fd = accept(server_fd, (struct sockaddr *)&address, &addrlen);
        if (client_fd < 0) {
            continue;
        }

        memset(buffer, 0, sizeof(buffer));
        ssize_t bytes_read = read(client_fd, buffer, sizeof(buffer) - 1);
        if (bytes_read <= 0) {
            close(client_fd);
            continue;
        }

        char method[16] = {0};
        char path[2048] = {0};

        if (sscanf(buffer, "%15s %2047s", method, path) != 2) {
            send_response(client_fd, 400, "Bad Request", "{\"error\":\"invalid request\"}");
            close(client_fd);
            continue;
        }

        if (strcmp(method, "GET") != 0) {
            send_response(client_fd, 405, "Method Not Allowed", "{\"error\":\"method not allowed\"}");
            close(client_fd);
            continue;
        }

        char *query = strchr(path, '?');
        if (strncmp(path, "/calculate-price", 16) != 0) {
            send_response(client_fd, 404, "Not Found", "{\"error\":\"not found\"}");
            close(client_fd);
            continue;
        }

        if (query == NULL) {
            send_response(client_fd, 400, "Bad Request", "{\"error\":\"missing parameters\"}");
            close(client_fd);
            continue;
        }

        *query++ = '\0';

        char product_code[64];
        char quantity_raw[64];

        get_query_param(query, "product_code", product_code, sizeof(product_code));
        get_query_param(query, "quantity", quantity_raw, sizeof(quantity_raw));

        for (size_t i = 0; product_code[i]; i++) {
            product_code[i] = (char)toupper((unsigned char)product_code[i]);
        }

        double unit_price;
        int quantity;

        if (!get_price(product_code, &unit_price)) {
            send_response(client_fd, 400, "Bad Request", "{\"error\":\"invalid product code\"}");
            close(client_fd);
            continue;
        }

        if (!parse_quantity(quantity_raw, &quantity)) {
            send_response(client_fd, 400, "Bad Request", "{\"error\":\"invalid quantity\"}");
            close(client_fd);
            continue;
        }

        char body[256];
        snprintf(
            body,
            sizeof(body),
            "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":\"%.2f\",\"total_price\":\"%.2f\"}",
            product_code,
            quantity,
            unit_price,
            unit_price * quantity
        );

        send_response(client_fd, 200, "OK", body);
        close(client_fd);
    }

    close(server_fd);
    return 0;
}