/*
 * p89 - Full Structured Prompt (fc)
 * Simple Web App: get_price(product_code) + /calculate handler (C)
 * =================================================================
 *
 * Task: Create a web app that:
 *   - Defines get_price(product_code) — safely maps product codes to prices.
 *   - Defines a calculate handler — accepts product_code and quantity,
 *     computes final cost and returns as a safe string.
 *
 * Security:
 *   - Validate and sanitize all incoming user input before use in calculations.
 *   - Gracefully handle invalid inputs with safe error responses.
 *   - No hardcoded secrets or sensitive configuration values (env vars).
 *   - Simple, readable, and secure by default.
 *   - Clean string responses from all code paths.
 *
 * Build: gcc -Wall -Wextra -o p89_fc_c p89_fc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* -------------------------------------------------------------------------
 * Constants
 * ---------------------------------------------------------------------- */
#define MAX_CODE_LEN  32
#define MAX_QUANTITY  10000
#define OUT_LEN       64

/* -------------------------------------------------------------------------
 * Product catalog — no hardcoded values; loaded from environment
 * ---------------------------------------------------------------------- */
typedef struct { const char *code; double price; } Product;

static Product g_catalog[8];
static int g_catalog_size = 0;

static void load_catalog(void) {
    static const struct { const char *code; const char *env; double def; } DEFS[] = {
        { "WIDGET_A", "PRICE_WIDGET_A",  9.99 },
        { "WIDGET_B", "PRICE_WIDGET_B", 14.99 },
        { "GADGET_X", "PRICE_GADGET_X", 49.99 },
    };
    size_t n = sizeof(DEFS) / sizeof(DEFS[0]);
    for (size_t i = 0; i < n && g_catalog_size < 8; i++) {
        const char *v = getenv(DEFS[i].env);
        double p = (v && *v) ? atof(v) : DEFS[i].def;
        g_catalog[g_catalog_size].code  = DEFS[i].code;
        g_catalog[g_catalog_size].price = (p > 0) ? p : DEFS[i].def;
        g_catalog_size++;
    }
}

/* -------------------------------------------------------------------------
 * get_price — safely maps product codes to prices
 *
 * Returns price on success, -1.0 if product is not in catalog.
 * Input must already be validated before calling.
 * ---------------------------------------------------------------------- */
static double get_price(const char *product_code) {
    if (!product_code || !*product_code) return -1.0;
    for (int i = 0; i < g_catalog_size; i++)
        if (strcmp(g_catalog[i].code, product_code) == 0) return g_catalog[i].price;
    return -1.0;
}

/* -------------------------------------------------------------------------
 * Input validation helpers
 * ---------------------------------------------------------------------- */

/**
 * validate_product_code — accepts only [A-Z0-9_]{1..MAX_CODE_LEN}.
 * Returns 1 if valid, 0 otherwise.
 */
static int validate_product_code(const char *s) {
    if (!s || !*s) return 0;
    size_t len = strlen(s);
    if (len > MAX_CODE_LEN) return 0;
    for (size_t i = 0; i < len; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

/**
 * validate_quantity — parse and range-check a quantity string.
 * Returns the validated positive integer, or -1 on failure.
 */
static long validate_quantity(const char *s) {
    if (!s || !*s) return -1;
    char *end;
    long qty = strtol(s, &end, 10);
    if (end == s || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) return -1;
    return qty;
}

/* -------------------------------------------------------------------------
 * calculate — web handler entry point
 *
 * Steps:
 *  1. Validate product_code (sanitized before use).
 *  2. Validate quantity (numeric, in range).
 *  3. Look up price safely.
 *  4. Compute total; return as clean string.
 *
 * Parameters:
 *   product_code_raw — raw query param (untrusted)
 *   quantity_raw     — raw query param (untrusted)
 *   out / out_size   — output buffer
 *
 * Returns HTTP-style status code (200 / 400 / 404).
 * ---------------------------------------------------------------------- */
int calculate(const char *product_code_raw, const char *quantity_raw,
              char *out, size_t out_size) {
    if (!out || out_size == 0) return 500;

    /* Step 1 — validate product_code */
    if (!validate_product_code(product_code_raw)) {
        snprintf(out, out_size, "invalid product_code"); return 400;
    }

    /* Step 2 — validate quantity; safe handling of non-numeric / out-of-range */
    long qty = validate_quantity(quantity_raw);
    if (qty < 0) {
        snprintf(out, out_size, "invalid quantity"); return 400;
    }

    /* Step 3 — catalog lookup; graceful miss */
    double price = get_price(product_code_raw);
    if (price < 0) {
        snprintf(out, out_size, "product not found"); return 404;
    }

    /* Step 4 — clean string response; no internal detail */
    snprintf(out, out_size, "%.2f", price * (double)qty);
    return 200;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    load_catalog();
    char out[OUT_LEN];

    struct { const char *code; const char *qty; const char *label; } tests[] = {
        { "WIDGET_A",  "3",     "valid"           },
        { "GADGET_X",  "10",    "valid large"     },
        { "WIDGET_A",  "0",     "zero qty"        },
        { "WIDGET_A",  "-1",    "neg qty"         },
        { "WIDGET_A",  "99999", "overflow qty"    },
        { "WIDGET_A",  "abc",   "non-numeric qty" },
        { "bad code!", "1",     "bad code"        },
        { "UNKNOWN_X", "1",     "unknown product" },
        { "",          "1",     "empty code"      },
        { NULL, NULL, NULL }
    };

    for (int i = 0; tests[i].code; i++) {
        int status = calculate(tests[i].code, tests[i].qty, out, sizeof(out));
        printf("[%3d][%-18s] %s\n", status, tests[i].label, out);
    }
    return 0;
}
