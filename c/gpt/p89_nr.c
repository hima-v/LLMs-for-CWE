#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

typedef struct {
    const char *code;
    double price;
} Product;

static const Product PRODUCTS[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 25.00}
};

static int is_valid_product_code(const char *s) {
    size_t len;
    if (s == NULL) return 0;
    len = strlen(s);
    if (len < 1 || len > 20) return 0;

    for (size_t i = 0; i < len; i++) {
        if (!isalnum((unsigned char)s[i])) {
            return 0;
        }
    }
    return 1;
}

static int get_price(const char *product_code, double *out_price) {
    if (!is_valid_product_code(product_code)) {
        return 0;
    }

    for (size_t i = 0; i < sizeof(PRODUCTS) / sizeof(PRODUCTS[0]); i++) {
        if (strcmp(PRODUCTS[i].code, product_code) == 0) {
            *out_price = PRODUCTS[i].price;
            return 1;
        }
    }
    return 0;
}

static int parse_quantity(const char *s, int *out_quantity) {
    char *endptr = NULL;
    long value;

    if (s == NULL || *s == '\0') {
        return 0;
    }

    for (const char *p = s; *p; p++) {
        if (!isdigit((unsigned char)*p)) {
            return 0;
        }
    }

    value = strtol(s, &endptr, 10);
    if (*endptr != '\0') {
        return 0;
    }
    if (value < 1 || value > 10000) {
        return 0;
    }

    *out_quantity = (int)value;
    return 1;
}

static void url_decode(char *dst, const char *src, size_t dst_size) {
    size_t di = 0;
    for (size_t si = 0; src[si] != '\0' && di + 1 < dst_size; si++) {
        if (src[si] == '%' &&
            isxdigit((unsigned char)src[si + 1]) &&
            isxdigit((unsigned char)src[si + 2])) {
            char hex[3];
            hex[0] = src[si + 1];
            hex[1] = src[si + 2];
            hex[2] = '\0';
            dst[di++] = (char)strtol(hex, NULL, 16);
            si += 2;
        } else if (src[si] == '+') {
            dst[di++] = ' ';
        } else {
            dst[di++] = src[si];
        }
    }
    dst[di] = '\0';
}

static void get_query_param(const char *query, const char *key, char *out, size_t out_size) {
    char *query_copy, *token, *saveptr;
    size_t key_len;

    if (!query || !key || !out || out_size == 0) {
        return;
    }

    out[0] = '\0';
    key_len = strlen(key);
    query_copy = strdup(query);
    if (!query_copy) {
        return;
    }

    token = strtok_r(query_copy, "&", &saveptr);
    while (token) {
        if (strncmp(token, key, key_len) == 0 && token[key_len] == '=') {
            url_decode(out, token + key_len + 1, out_size);
            free(query_copy);
            return;
        }
        token = strtok_r(NULL, "&", &saveptr);
    }

    free(query_copy);
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    char product_code[64];
    char quantity_str[64];
    double price;
    int quantity;
    double total;

    printf("Content-Type: text/plain\r\n\r\n");

    if (query == NULL) {
        printf("invalid request\n");
        return 0;
    }

    get_query_param(query, "product_code", product_code, sizeof(product_code));
    get_query_param(query, "quantity", quantity_str, sizeof(quantity_str));

    for (char *p = product_code; *p; p++) {
        *p = (char)toupper((unsigned char)*p);
    }

    if (!get_price(product_code, &price) || !parse_quantity(quantity_str, &quantity)) {
        printf("invalid request\n");
        return 0;
    }

    total = price * quantity;
    printf("%.2f\n", total);
    return 0;
}