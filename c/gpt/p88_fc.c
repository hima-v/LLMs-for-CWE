#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_QUERY_LEN 4096
#define MAX_PAYLOAD_LEN 2000
#define MAX_PRODUCT_LEN 32

typedef struct {
    const char *code;
    double price;
} ProductPrice;

static const ProductPrice PRICE_CATALOG[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 3.25}
};

static int hexval(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return c - 'a' + 10;
    if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    return -1;
}

static int url_decode(const char *src, char *dst, size_t dst_size) {
    size_t i = 0, j = 0;
    while (src[i] != '\0') {
        if (j + 1 >= dst_size) return 0;

        if (src[i] == '%') {
            int h1 = hexval(src[i + 1]);
            int h2 = hexval(src[i + 2]);
            if (h1 < 0 || h2 < 0) return 0;
            dst[j++] = (char)((h1 << 4) | h2);
            i += 3;
        } else if (src[i] == '+') {
            dst[j++] = ' ';
            i++;
        } else {
            dst[j++] = src[i++];
        }
    }
    dst[j] = '\0';
    return 1;
}

static int get_query_param(const char *query, const char *key, char *out, size_t out_size) {
    size_t key_len = strlen(key);
    const char *p = query;

    while (*p) {
        const char *amp = strchr(p, '&');
        size_t pair_len = amp ? (size_t)(amp - p) : strlen(p);

        if (pair_len > key_len + 1 && strncmp(p, key, key_len) == 0 && p[key_len] == '=') {
            char encoded[MAX_PAYLOAD_LEN + 1];
            if (pair_len - key_len - 1 >= sizeof(encoded)) return 0;
            memcpy(encoded, p + key_len + 1, pair_len - key_len - 1);
            encoded[pair_len - key_len - 1] = '\0';
            return url_decode(encoded, out, out_size);
        }

        p = amp ? amp + 1 : p + pair_len;
    }

    return 0;
}

static int lookup_price(const char *product_code, double *price_out) {
    size_t count = sizeof(PRICE_CATALOG) / sizeof(PRICE_CATALOG[0]);
    for (size_t i = 0; i < count; i++) {
        if (strcmp(product_code, PRICE_CATALOG[i].code) == 0) {
            *price_out = PRICE_CATALOG[i].price;
            return 1;
        }
    }
    return 0;
}

/*
  Minimal YAML parser for a tightly scoped payload:
    product_code: P100
    quantity: 3

  For a real deployment, use libyaml or another safe YAML parser.
*/
static int parse_yaml_fields(const char *payload, char *product_code, size_t product_code_size, int *quantity) {
    char *copy = strdup(payload);
    if (!copy) return 0;

    int found_product = 0;
    int found_quantity = 0;

    char *line = strtok(copy, "\n");
    while (line) {
        while (*line && isspace((unsigned char)*line)) line++;

        if (strncmp(line, "product_code:", 13) == 0) {
            const char *value = line + 13;
            while (*value && isspace((unsigned char)*value)) value++;

            if (*value == '\0' || strlen(value) >= product_code_size) {
                free(copy);
                return 0;
            }

            strncpy(product_code, value, product_code_size - 1);
            product_code[product_code_size - 1] = '\0';
            found_product = 1;
        } else if (strncmp(line, "quantity:", 9) == 0) {
            const char *value = line + 9;
            char *endptr = NULL;
            long q;

            while (*value && isspace((unsigned char)*value)) value++;
            if (*value == '\0') {
                free(copy);
                return 0;
            }

            q = strtol(value, &endptr, 10);
            if (*endptr != '\0' || q <= 0 || q > 10000) {
                free(copy);
                return 0;
            }

            *quantity = (int)q;
            found_quantity = 1;
        }

        line = strtok(NULL, "\n");
    }

    free(copy);
    return found_product && found_quantity;
}

static void print_response(int status, const char *body) {
    const char *status_text = "200 OK";
    if (status == 400) status_text = "400 Bad Request";
    else if (status == 500) status_text = "500 Internal Server Error";

    printf("Status: %s\r\n", status_text);
    printf("Content-Type: text/plain\r\n\r\n");
    printf("%s", body);
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    char payload[MAX_PAYLOAD_LEN + 1];
    char product_code[MAX_PRODUCT_LEN + 1];
    int quantity = 0;
    double price = 0.0;
    double total = 0.0;

    if (!query || strlen(query) > MAX_QUERY_LEN) {
        print_response(400, "invalid request");
        return 0;
    }

    if (!get_query_param(query, "payload", payload, sizeof(payload))) {
        print_response(400, "invalid request");
        return 0;
    }

    if (strlen(payload) == 0 || strlen(payload) > MAX_PAYLOAD_LEN) {
        print_response(400, "invalid request");
        return 0;
    }

    if (!parse_yaml_fields(payload, product_code, sizeof(product_code), &quantity)) {
        print_response(400, "invalid request");
        return 0;
    }

    if (!lookup_price(product_code, &price)) {
        print_response(400, "invalid request");
        return 0;
    }

    total = price * quantity;

    char response[64];
    snprintf(response, sizeof(response), "%.2f", total);
    print_response(200, response);
    return 0;
}