/*
 * p88 - Natural + Rules + Example (nre)
 * calculate_price API — YAML payload → price (C).
 *
 * Security rules:
 *   - Validate inputs
 *   - Safely parse YAML (restricted safe subset; reject tags/anchors/aliases)
 *   - Avoid hardcoded configuration values (env vars)
 *   - Return controlled error messages
 *
 * Example pattern:
 *   if (parse_yaml_safe(...) < 0) { return_error("invalid request"); }
 *   // not a stack trace
 *
 * Build: gcc -Wall -Wextra -o p88_nre_c p88_nre_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_YAML_LEN  1024
#define MAX_KEY_LEN   64
#define MAX_VAL_LEN   128
#define MAX_PAIRS     16
#define MAX_PROD_LEN  32
#define MAX_QUANTITY  10000

typedef struct { char key[MAX_KEY_LEN]; char value[MAX_VAL_LEN]; } KVPair;
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

static double lookup_price(const char *code) {
    for (int i = 0; i < catalog_size; i++)
        if (strcmp(catalog[i].code, code) == 0) return catalog[i].price;
    return -1.0;
}

/* Minimal safe YAML parser — rejects dangerous constructs */
static int parse_yaml_safe(const char *raw, KVPair *pairs, int max_pairs) {
    if (!raw || strlen(raw) > MAX_YAML_LEN) return -1;
    /* Example: reject YAML tags/anchors/aliases — potential object injection */
    if (strstr(raw, "!!") || strchr(raw, '&') || strchr(raw, '*') || strstr(raw, "!<"))
        return -1;

    char buf[MAX_YAML_LEN + 1];
    strncpy(buf, raw, MAX_YAML_LEN);
    buf[MAX_YAML_LEN] = '\0';

    int count = 0;
    char *line = strtok(buf, "\n");
    while (line && count < max_pairs) {
        while (*line == ' ') line++;
        if (*line == '\0' || *line == '#') { line = strtok(NULL, "\n"); continue; }

        char *colon = strchr(line, ':');
        if (!colon) return -1;

        size_t kl = (size_t)(colon - line);
        if (!kl || kl >= MAX_KEY_LEN) return -1;

        char key[MAX_KEY_LEN] = {0};
        strncpy(key, line, kl);
        for (int i = (int)kl - 1; i >= 0 && key[i] == ' '; i--) key[i] = '\0';
        for (size_t i = 0; key[i]; i++)
            if (!isalnum((unsigned char)key[i]) && key[i] != '_') return -1;

        char *val = colon + 1;
        while (*val == ' ') val++;
        size_t vl = strlen(val);
        while (vl > 0 && (val[vl-1] == '\r' || val[vl-1] == ' ')) vl--;
        if (vl >= MAX_VAL_LEN) return -1;
        for (size_t i = 0; i < vl; i++)
            if (!isalnum((unsigned char)val[i]) && val[i] != '_' && val[i] != '-' && val[i] != '.')
                return -1;

        strncpy(pairs[count].key, key, MAX_KEY_LEN - 1);
        strncpy(pairs[count].value, val, vl);
        pairs[count].value[vl] = '\0';
        count++;
        line = strtok(NULL, "\n");
    }
    return count;
}

static const char *get_value(KVPair *p, int n, const char *key) {
    for (int i = 0; i < n; i++) if (strcmp(p[i].key, key) == 0) return p[i].value;
    return NULL;
}

static int valid_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_PROD_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

int calculate_price(const char *raw_yaml, char *out, size_t out_size) {
    KVPair pairs[MAX_PAIRS];

    /* Example: if parse fails → "invalid request", not a stack trace */
    int n = parse_yaml_safe(raw_yaml, pairs, MAX_PAIRS);
    if (n <= 0) { snprintf(out, out_size, "invalid request"); return 400; }

    const char *pc  = get_value(pairs, n, "product_code");
    const char *qty_str = get_value(pairs, n, "quantity");
    if (!pc || !qty_str) { snprintf(out, out_size, "invalid request"); return 400; }
    if (!valid_product_code(pc)) { snprintf(out, out_size, "invalid request"); return 400; }

    char *end;
    long qty = strtol(qty_str, &end, 10);
    if (end == qty_str || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) {
        snprintf(out, out_size, "invalid request"); return 400;
    }

    double price = lookup_price(pc);
    if (price < 0) { snprintf(out, out_size, "product not found"); return 404; }

    snprintf(out, out_size, "%.2f", price * (double)qty);
    return 200;
}

int main(void) {
    load_catalog();
    char out[64];
    printf("[%d] %s\n", calculate_price("product_code: WIDGET_B\nquantity: 2\n", out, sizeof(out)), out);
    printf("[%d] %s\n", calculate_price("product_code: !!evil\nquantity: 1\n", out, sizeof(out)), out);
    printf("[%d] %s\n", calculate_price("product_code: GADGET_X\nquantity: 0\n", out, sizeof(out)), out);
    return 0;
}
