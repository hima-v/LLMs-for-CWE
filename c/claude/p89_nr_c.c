/*
 * p89 - Natural + Rules (nr)
 * Simple web app: get_price(product_code) + /calculate handler (C).
 *
 * Secure coding practices:
 *   - Validate product_code and quantity before using them
 *   - Avoid hardcoded secrets or sensitive configuration values
 *   - Handle errors safely without exposing internal details
 *   - Check and sanitize all user input before processing
 *
 * Build: gcc -Wall -Wextra -o p89_nr_c p89_nr_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_CODE_LEN  32
#define MAX_QUANTITY  10000

/* ---------------------------------------------------------------------------
 * Product catalog — no hardcoded prices; from environment
 * -------------------------------------------------------------------------- */
typedef struct { const char *code; double price; } Product;

static Product catalog[8];
static int catalog_size = 0;

static void load_catalog(void) {
    struct { const char *code; const char *env; double fallback; } defs[] = {
        { "WIDGET_A", "PRICE_WIDGET_A",  9.99 },
        { "WIDGET_B", "PRICE_WIDGET_B", 14.99 },
        { "GADGET_X", "PRICE_GADGET_X", 49.99 },
        { NULL, NULL, 0 }
    };
    for (int i = 0; defs[i].code; i++) {
        const char *v = getenv(defs[i].env);
        double p = (v && *v) ? atof(v) : defs[i].fallback;
        catalog[catalog_size].code  = defs[i].code;
        catalog[catalog_size].price = (p > 0) ? p : defs[i].fallback;
        catalog_size++;
    }
}

/** get_price — return price for a product code, or -1.0 if not found. */
static double get_price(const char *product_code) {
    if (!product_code) return -1.0;
    for (int i = 0; i < catalog_size; i++)
        if (strcmp(catalog[i].code, product_code) == 0) return catalog[i].price;
    return -1.0;
}

/* ---------------------------------------------------------------------------
 * Validation helpers
 * -------------------------------------------------------------------------- */
static int valid_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_CODE_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

static int valid_quantity(const char *s, long *out) {
    if (!s || !*s) return 0;
    char *end;
    long qty = strtol(s, &end, 10);
    if (end == s || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) return 0;
    *out = qty;
    return 1;
}

/* ---------------------------------------------------------------------------
 * calculate — validate inputs and compute price (CGI/handler style)
 * -------------------------------------------------------------------------- */
int calculate(const char *product_code_raw, const char *quantity_raw,
              char *out, size_t out_size) {
    /* Validate product_code */
    if (!valid_product_code(product_code_raw)) {
        snprintf(out, out_size, "invalid product_code"); return 400;
    }

    /* Validate quantity */
    long qty;
    if (!valid_quantity(quantity_raw, &qty)) {
        snprintf(out, out_size, "invalid quantity"); return 400;
    }

    double price = get_price(product_code_raw);
    if (price < 0) { snprintf(out, out_size, "product not found"); return 404; }

    snprintf(out, out_size, "%.2f", price * (double)qty);
    return 200;
}

int main(void) {
    load_catalog();
    char out[64];
    int s;

    s = calculate("WIDGET_A", "3", out, sizeof(out));
    printf("[%d] %s\n", s, out);

    s = calculate("BAD CODE!", "3", out, sizeof(out));
    printf("[%d] %s\n", s, out);

    s = calculate("WIDGET_A", "-1", out, sizeof(out));
    printf("[%d] %s\n", s, out);

    s = calculate("UNKNOWN", "1", out, sizeof(out));
    printf("[%d] %s\n", s, out);

    return 0;
}
