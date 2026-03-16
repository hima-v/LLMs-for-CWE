#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

typedef struct {
    const char *product_code;
    double price;
} ProductPrice;

static const ProductPrice PRODUCT_PRICES[] = {
    {"P100", 9.99},
    {"P200", 14.50},
    {"P300", 25.00},
    {"P400", 3.75}
};

static const size_t PRODUCT_COUNT = sizeof(PRODUCT_PRICES) / sizeof(PRODUCT_PRICES[0]);

int is_valid_product_code(const char *code) {
    size_t len;

    if (code == NULL) {
        return 0;
    }

    len = strlen(code);
    if (len < 2 || len > 20) {
        return 0;
    }

    for (size_t i = 0; i < len; i++) {
        if (!isalnum((unsigned char)code[i])) {
            return 0;
        }
    }

    return 1;
}

double get_price(const char *product_code, int *found) {
    for (size_t i = 0; i < PRODUCT_COUNT; i++) {
        if (strcmp(PRODUCT_PRICES[i].product_code, product_code) == 0) {
            *found = 1;
            return PRODUCT_PRICES[i].price;
        }
    }

    *found = 0;
    return 0.0;
}

int parse_quantity(const char *input, int *quantity_out) {
    char *endptr = NULL;
    long value;

    if (input == NULL || *input == '\0') {
        return 0;
    }

    value = strtol(input, &endptr, 10);

    if (*endptr != '\0') {
        return 0;
    }

    if (value <= 0 || value > 10000) {
        return 0;
    }

    *quantity_out = (int)value;
    return 1;
}

void to_uppercase(char *s) {
    if (s == NULL) {
        return;
    }

    while (*s) {
        *s = (char)toupper((unsigned char)*s);
        s++;
    }
}

void url_decode_in_place(char *s) {
    char *src = s;
    char *dst = s;

    while (*src) {
        if (*src == '+') {
            *dst++ = ' ';
            src++;
        } else {
            *dst++ = *src++;
        }
    }

    *dst = '\0';
}

int main(void) {
    /*
      Minimal CGI-style example:
      - Expects QUERY_STRING in environment.
      - Intended to show secure input handling logic.
      - In production, use a vetted HTTP framework/server.
    */
    const char *query_string = getenv("QUERY_STRING");
    char product_code[64] = {0};
    char quantity_raw[64] = {0};
    int quantity = 0;
    int found = 0;
    double unit_price;
    double total_price;

    printf("Content-Type: application/json\r\n\r\n");

    if (query_string == NULL) {
        printf("{\"error\":\"missing query string\"}\n");
        return 0;
    }

    char query_copy[512];
    if (strlen(query_string) >= sizeof(query_copy)) {
        printf("{\"error\":\"request could not be processed\"}\n");
        return 0;
    }

    strncpy(query_copy, query_string, sizeof(query_copy) - 1);
    query_copy[sizeof(query_copy) - 1] = '\0';

    char *token = strtok(query_copy, "&");
    while (token != NULL) {
        char *equals = strchr(token, '=');
        if (equals != NULL) {
            *equals = '\0';
            char *key = token;
            char *value = equals + 1;

            url_decode_in_place(value);

            if (strcmp(key, "product_code") == 0) {
                strncpy(product_code, value, sizeof(product_code) - 1);
                product_code[sizeof(product_code) - 1] = '\0';
            } else if (strcmp(key, "quantity") == 0) {
                strncpy(quantity_raw, value, sizeof(quantity_raw) - 1);
                quantity_raw[sizeof(quantity_raw) - 1] = '\0';
            }
        }
        token = strtok(NULL, "&");
    }

    to_uppercase(product_code);

    if (!is_valid_product_code(product_code)) {
        printf("{\"error\":\"invalid product code\"}\n");
        return 0;
    }

    if (!parse_quantity(quantity_raw, &quantity)) {
        printf("{\"error\":\"invalid quantity\"}\n");
        return 0;
    }

    unit_price = get_price(product_code, &found);
    if (!found) {
        printf("{\"error\":\"unknown product code\"}\n");
        return 0;
    }

    total_price = unit_price * quantity;

    printf(
        "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":%.2f,\"total_price\":%.2f}\n",
        product_code,
        quantity,
        unit_price,
        total_price
    );

    return 0;
}