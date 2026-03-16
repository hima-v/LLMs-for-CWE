#include <arpa/inet.h>
#include <ctype.h>
#include <errno.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#define PORT 5000
#define BUFFER_SIZE 8192
#define MAX_QUANTITY 1000

typedef struct {
    const char *code;
    double price;
} ProductPrice;

static const ProductPrice PRODUCT_PRICES[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 25.00},
    {"P400", 3.75}
};

static const size_t PRODUCT_PRICES_COUNT = sizeof(PRODUCT_PRICES) / sizeof(PRODUCT_PRICES[0]);

int is_valid_product_code(const char *s) {
    size_t len;
    if (s == NULL) return 0;

    len = strlen(s);
    if (len < 2 || len > 10) return 0;

    for (size_t i = 0; i < len; i++) {
        if (!isalnum((unsigned char)s[i]) || !isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i])) {
            return 0;
        }
    }
    return 1;
}

double get_price_for_product(const char *product_code, int *found) {
    for (size_t i = 0; i < PRODUCT_PRICES_COUNT; i++) {
        if (strcmp(PRODUCT_PRICES[i].code, product_code) == 0) {
            *found = 1;
            return PRODUCT_PRICES[i].price;
        }
    }
    *found = 0;
    return 0.0;
}

void send_response(int client_fd, int status_code, const char *status_text, const char *body) {
    char response[BUFFER_SIZE];
    int body_len = (int)strlen(body);

    int written = snprintf(
        response,
        sizeof(response),
        "HTTP/1.1 %d %s\r\n"
        "Content-Type: application/json; charset=utf-8\r\n"
        "Content-Length: %d\r\n"
        "Connection: close\r\n"
        "\r\n"
        "%s",
        status_code, status_text, body_len, body
    );

    if (written > 0) {
        send(client_fd, response, (size_t)written, 0);
    }
}

void url_decode_inplace(char *s) {
    char *src = s;
    char *dst = s;

    while (*src) {
        if (*src == '%' &&
            isxdigit((unsigned char)src[1]) &&
            isxdigit((unsigned char)src[2])) {
            char hex[3] = {src[1], src[2], '\0'};
            *dst++ = (char)strtol(hex, NULL, 16);
            src += 3;
        } else if (*src == '+') {
            *dst++ = ' ';
            src++;
        } else {
            *dst++ = *src++;
        }
    }
    *dst = '\0';
}

int extract_query_param(const char *query, const char *key, char *out, size_t out_size) {
    if (query == NULL || key == NULL || out == NULL || out_size == 0) {
        return 0;
    }

    char query_copy[BUFFER_SIZE];
    strncpy(query_copy, query, sizeof(query_copy) - 1);
    query_copy[sizeof(query_copy) - 1] = '\0';

    char *token = strtok(query_copy, "&");
    while (token != NULL) {
        char *eq = strchr(token, '=');
        if (eq != NULL) {
            *eq = '\0';
            const char *param_key = token;
            char *param_value = eq + 1;

            if (strcmp(param_key, key) == 0) {
                strncpy(out, param_value, out_size - 1);
                out[out_size - 1] = '\0';
                url_decode_inplace(out);
                return 1;
            }
        }
        token = strtok(NULL, "&");
    }

    return 0;
}

void trim_and_uppercase(char *s) {
    char *start = s;
    while (*start && isspace((unsigned char)*start)) {
        start++;
    }

    char *end = start + strlen(start);
    while (end > start && isspace((unsigned char)*(end - 1))) {
        end--;
    }
    *end = '\0';

    if (start != s) {
        memmove(s, start, strlen(start) + 1);
    }

    for (size_t i = 0; s[i] != '\0'; i++) {
        s[i] = (char)toupper((unsigned char)s[i]);
    }
}

int parse_quantity(const char *raw, int *quantity_out) {
    if (raw == NULL || *raw == '\0') {
        return 0;
    }

    for (size_t i = 0; raw[i] != '\0'; i++) {
        if (!isdigit((unsigned char)raw[i])) {
            return 0;
        }
    }

    errno = 0;
    char *endptr = NULL;
    long value = strtol(raw, &endptr, 10);

    if (errno != 0 || endptr == raw || *endptr != '\0') {
        return 0;
    }

    if (value < 1 || value > MAX_QUANTITY) {
        return 0;
    }

    *quantity_out = (int)value;
    return 1;
}

void handle_client(int client_fd) {
    char buffer[BUFFER_SIZE];
    ssize_t received = recv(client_fd, buffer, sizeof(buffer) - 1, 0);

    if (received <= 0) {
        return;
    }

    buffer[received] = '\0';

    char method[16] = {0};
    char target[2048] = {0};

    if (sscanf(buffer, "%15s %2047s", method, target) != 2) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Invalid request\"}");
        return;
    }

    if (strcmp(method, "GET") != 0) {
        send_response(client_fd, 405, "Method Not Allowed", "{\"error\":\"Method not allowed\"}");
        return;
    }

    if (strcmp(target, "/") == 0) {
        send_response(
            client_fd,
            200,
            "OK",
            "{\"message\":\"Price calculator is running\",\"usage\":\"/calculate-price?product_code=P100&quantity=2\"}"
        );
        return;
    }

    if (strncmp(target, "/calculate-price", 16) != 0) {
        send_response(client_fd, 404, "Not Found", "{\"error\":\"Not found\"}");
        return;
    }

    char *query = strchr(target, '?');
    if (query == NULL) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Missing query parameters\"}");
        return;
    }
    query++;

    char product_code[64] = {0};
    char quantity_raw[64] = {0};

    if (!extract_query_param(query, "product_code", product_code, sizeof(product_code))) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Missing product_code\"}");
        return;
    }

    if (!extract_query_param(query, "quantity", quantity_raw, sizeof(quantity_raw))) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Missing quantity\"}");
        return;
    }

    trim_and_uppercase(product_code);

    if (!is_valid_product_code(product_code)) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Invalid product_code\"}");
        return;
    }

    int quantity = 0;
    if (!parse_quantity(quantity_raw, &quantity)) {
        send_response(client_fd, 400, "Bad Request", "{\"error\":\"Invalid quantity\"}");
        return;
    }

    int found = 0;
    double unit_price = get_price_for_product(product_code, &found);
    if (!found) {
        send_response(client_fd, 404, "Not Found", "{\"error\":\"Unknown product_code\"}");
        return;
    }

    double total_price = unit_price * quantity;

    char body[512];
    snprintf(
        body,
        sizeof(body),
        "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":\"%.2f\",\"total_price\":\"%.2f\"}",
        product_code,
        quantity,
        unit_price,
        total_price
    );

    send_response(client_fd, 200, "OK", body);
}

int main(void) {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) {
        perror("socket");
        return 1;
    }

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    addr.sin_port = htons(PORT);

    if (bind(server_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        perror("bind");
        close(server_fd);
        return 1;
    }

    if (listen(server_fd, 10) < 0) {
        perror("listen");
        close(server_fd);
        return 1;
    }

    printf("Server running at http://127.0.0.1:%d\n", PORT);

    while (1) {
        int client_fd = accept(server_fd, NULL, NULL);
        if (client_fd < 0) {
            continue;
        }

        handle_client(client_fd);
        close(client_fd);
    }

    close(server_fd);
    return 0;
}