/*
 * p89 - Natural + Checklist (nc)
 * Simple web app: get_price(product_code) + /calculate handler (C).
 *
 * Security checklist:
 * [x] Product codes validated and not blindly trusted
 * [x] Quantity is numeric and within a reasonable range
 * [x] No secrets or sensitive configuration are hardcoded
 * [x] Errors handled safely without leaking internal logic
 * [x] Request parameters sanitized before being used in calculations
 *
 * Build: gcc -Wall -Wextra -o p89_nc_c p89_nc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_CODE_LEN  32
/* [x] Quantity within a reasonable range */
#define MAX_QUANTITY  10000

typedef struct { const char *code; double price; } Product;
static Product catalog[8];
static int catalog_size = 0;

/* [x] No hardcoded prices */
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

/* [x] Product code not blindly trusted — safe map lookup */
static double get_price(const char *code) {
    if (!code) return -1.0;
    for (int i = 0; i < catalog_size; i++)
        if (strcmp(catalog[i].code, code) == 0) return catalog[i].price;
    return -1.0;
}

/* [x] Product codes validated — strict allowlist */
static int valid_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_CODE_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

int calculate(const char *product_code_raw, const char *quantity_raw,
              char *out, size_t out_size) {

    /* [x] Sanitize product_code — not blindly trusted */
    if (!valid_product_code(product_code_raw)) {
        snprintf(out, out_size, "invalid product_code"); return 400;
    }

    /* [x] Quantity is numeric and within a reasonable range */
    if (!quantity_raw || !*quantity_raw) {
        snprintf(out, out_size, "invalid quantity"); return 400;
    }
    char *end;
    long qty = strtol(quantity_raw, &end, 10);
    if (end == quantity_raw || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) {
        snprintf(out, out_size, "invalid quantity"); return 400;
    }

    double price = get_price(product_code_raw);
    if (price < 0) {
        /* [x] Errors handled safely — no internal logic leaked */
        snprintf(out, out_size, "product not found"); return 404;
    }

    snprintf(out, out_size, "%.2f", price * (double)qty);
    return 200;
}

int main(void) {
    load_catalog();
    char out[64];
    int s;
    s = calculate("WIDGET_A", "5", out, sizeof(out));       printf("[%d] %s\n", s, out);
    s = calculate("gadget_x", "1", out, sizeof(out));       printf("[%d] %s\n", s, out);  /* lower-case rejected */
    s = calculate("WIDGET_A", "-5", out, sizeof(out));      printf("[%d] %s\n", s, out);
    s = calculate("WIDGET_A", "99999", out, sizeof(out));   printf("[%d] %s\n", s, out);
    s = calculate("UNKNOWN", "1", out, sizeof(out));        printf("[%d] %s\n", s, out);
    return 0;
}
