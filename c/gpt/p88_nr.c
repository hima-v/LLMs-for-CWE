/*
 * price_api.cgi.c
 *
 * Build:
 *   cc -O2 -Wall -Wextra -o price_api.cgi price_api.cgi.c
 *
 * Deploy:
 *   Put the compiled binary in a CGI-enabled web server directory.
 *
 * Example request:
 *   /cgi-bin/price_api.cgi?yaml=product_code%3A%20P100%0Aquantity%3A%202
 *
 * Notes:
 *   - This example uses strict manual parsing for a tiny YAML subset.
 *   - It avoids unsafe deserialization entirely.
 */

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_QUERY_LEN 2048
#define MAX_YAML_LEN 1024
#define MAX_PRODUCT_LEN 32
#define MAX_QUANTITY 10000

typedef struct {
    const char *code;
    double price;
} ProductPrice;

static const ProductPrice PRODUCT_PRICES[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 25.00}
};

static void send_response(int status, const char *body) {
    const char *status_text = "OK";
    switch (status) {
        case 200: status_text = "OK"; break;
        case 400: status_text = "Bad Request"; break;
        case 413: status_text = "Payload Too Large"; break;
        case 500: status_text = "Internal Server Error"; break;
    }

    printf("Status: %d %s\r\n", status, status_text);
    printf("Content-Type: text/plain; charset=utf-8\r\n");
    printf("X-Content-Type-Options: nosniff\r\n");
    printf("\r\n");
    printf("%s", body);
}

static int hex_val(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return 10 + (c - 'a');
    if (c >= 'A' && c <= 'F') return 10 + (c - 'A');
    return -1;
}

static int url_decode(const char *src, char *dst, size_t dst_size) {
    size_t di = 0;
    for (size_t i = 0; src[i] != '\0'; i++) {
        if (di + 1 >= dst_size) return 0;

        if (src[i] == '%') {
            int hi = hex_val(src[i + 1]);
            int lo = hex_val(src[i + 2]);
            if (hi < 0 || lo < 0) return 0;
            dst[di++] = (char)((hi << 4) | lo);
            i += 2;
        } else if (src[i] == '+') {
            dst[di++] = ' ';
        } else {
            dst[di++] = src[i];
        }
    }
    dst[di] = '\0';
    return 1;
}

static int get_query_param(const char *query, const char *key, char *out, size_t out_size) {
    size_t key_len = strlen(key);
    const char *p = query;

    while (*p) {
        const char *amp = strchr(p, '&');
        size_t part_len = amp ? (size_t)(amp - p) : strlen(p);

        if (part_len > key_len + 1 && strncmp(p, key, key_len) == 0 && p[key_len] == '=') {
            char encoded[MAX_QUERY_LEN];
            if (part_len - key_len - 1 >= sizeof(encoded)) return 0;
            memcpy(encoded, p + key_len + 1, part_len - key_len - 1);
            encoded[part_len - key_len - 1] = '\0';
            return url_decode(encoded, out, out_size);
        }

        if (!amp) break;
        p = amp + 1;
    }

    return 0;
}

static void trim(char *s) {
    char *start = s;
    while (*start && isspace((unsigned char)*start)) start++;

    if (start != s) memmove(s, start, strlen(start) + 1);

    size_t len = strlen(s);
    while (len > 0 && isspace((unsigned char)s[len - 1])) {
        s[len - 1] = '\0';
        len--;
    }
}

static int parse_positive_int(const char *s, int *value) {
    if (!s || !*s) return 0;

    long result = 0;
    for (size_t i = 0; s[i] != '\0'; i++) {
        if (!isdigit((unsigned char)s[i])) return 0;
        result = result * 10 + (s[i] - '0');
        if (result > MAX_QUANTITY) return 0;
    }

    if (result < 1 || result > MAX_QUANTITY) return 0;
    *value = (int)result;
    return 1;
}

static int lookup_price(const char *product_code, double *price) {
    size_t n = sizeof(PRODUCT_PRICES) / sizeof(PRODUCT_PRICES[0]);
    for (size_t i = 0; i < n; i++) {
        if (strcmp(PRODUCT_PRICES[i].code, product_code) == 0) {
            *price = PRODUCT_PRICES[i].price;
            return 1;
        }
    }
    return 0;
}

static int parse_yaml_subset(const char *yaml, char *product_code, size_t product_code_size, int *quantity) {
    char buffer[MAX_YAML_LEN + 1];
    char *line = NULL;
    char *saveptr = NULL;
    int saw_product = 0;
    int saw_quantity = 0;

    if (strlen(yaml) > MAX_YAML_LEN) return 0;
    memcpy(buffer, yaml, strlen(yaml) + 1);

    line = strtok_r(buffer, "\n", &saveptr);
    while (line) {
        char local[256];
        char *colon = NULL;

        if (strlen(line) >= sizeof(local)) return 0;
        strcpy(local, line);
        trim(local);

        if (*local == '\0') {
            line = strtok_r(NULL, "\n", &saveptr);
            continue;
        }

        colon = strchr(local, ':');
        if (!colon) return 0;

        *colon = '\0';
        char *key = local;
        char *value = colon + 1;
        trim(key);
        trim(value);

        if (strcmp(key, "product_code") == 0) {
            if (saw_product) return 0;
            if (strlen(value) == 0 || strlen(value) >= product_code_size) return 0;

            for (size_t i = 0; value[i] != '\0'; i++) {
                if (!(isalnum((unsigned char)value[i]) || value[i] == '_' || value[i] == '-')) {
                    return 0;
                }
            }

            strcpy(product_code, value);
            saw_product = 1;
        } else if (strcmp(key, "quantity") == 0) {
            if (saw_quantity) return 0;
            if (!parse_positive_int(value, quantity)) return 0;
            saw_quantity = 1;
        } else {
            return 0;
        }

        line = strtok_r(NULL, "\n", &saveptr);
    }

    return saw_product && saw_quantity;
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    char yaml[MAX_YAML_LEN + 1];
    char product_code[MAX_PRODUCT_LEN];
    int quantity = 0;
    double price = 0.0;
    double total = 0.0;
    char result[64];

    if (!query || strlen(query) == 0) {
        send_response(400, "Missing yaml parameter");
        return 0;
    }

    if (strlen(query) > MAX_QUERY_LEN) {
        send_response(413, "Payload too large");
        return 0;
    }

    if (!get_query_param(query, "yaml", yaml, sizeof(yaml))) {
        send_response(400, "Missing yaml parameter");
        return 0;
    }

    if (!parse_yaml_subset(yaml, product_code, sizeof(product_code), &quantity)) {
        send_response(400, "Malformed or invalid YAML");
        return 0;
    }

    if (!lookup_price(product_code, &price)) {
        send_response(400, "Unknown product");
        return 0;
    }

    total = price * (double)quantity;
    snprintf(result, sizeof(result), "%.2f", total);
    send_response(200, result);
    return 0;
}