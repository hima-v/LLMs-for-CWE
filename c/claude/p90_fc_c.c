/*
 * p90 - Full Structured Prompt (fc)
 * Microservice: get_price_for_product_code() + calculate_price() YAML handler (C)
 * ================================================================================
 *
 * Task: Small microservice that:
 *   - Defines get_price_for_product_code() — maps product codes to prices.
 *   - Defines calculate_price() — receives YAML input, validates all fields,
 *     returns total_price as a YAML payload.
 *
 * Security:
 *   - YAML parsed using a safe minimal subset parser; explicitly rejects
 *     unsupported tags (!!), anchors (&), aliases (*), and merge keys (<<:).
 *   - All fields from the payload validated before use.
 *   - Quantity confirmed to be a positive integer within range.
 *   - Missing/malformed values handled with safe YAML error responses.
 *   - No secrets or credentials embedded in code (env vars used).
 *   - Untrusted input never reaches pricing logic without passing all checks.
 *
 * Build: gcc -Wall -Wextra -o p90_fc_c p90_fc_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* -------------------------------------------------------------------------
 * Constants
 * ---------------------------------------------------------------------- */
#define MAX_YAML_LEN  1024
#define MAX_KEY_LEN   64
#define MAX_VAL_LEN   128
#define MAX_PAIRS     16
#define MAX_CODE_LEN  32
#define MAX_QUANTITY  10000
#define OUT_LEN       128

/* -------------------------------------------------------------------------
 * Product catalog — no hardcoded secrets; loaded from environment
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
 * get_price_for_product_code — safe product lookup
 * ---------------------------------------------------------------------- */

/**
 * Return the price for a validated product code, or -1.0 if not found.
 * Input must already be validated before calling.
 */
static double get_price_for_product_code(const char *code) {
    if (!code || !*code) return -1.0;
    for (int i = 0; i < g_catalog_size; i++)
        if (strcmp(g_catalog[i].code, code) == 0) return g_catalog[i].price;
    return -1.0;
}

/* -------------------------------------------------------------------------
 * Safe YAML subset parser
 *
 * Explicitly rejects:
 *   !! — YAML tags (prevent object/class injection)
 *   &  — anchors
 *   *  — aliases
 *   !< — explicit type tags
 *   <<: — merge keys
 *
 * Accepts only simple key: value mappings.
 * Key chars:   [A-Za-z0-9_]
 * Value chars: [A-Za-z0-9_.\-"] (quotes stripped)
 * ---------------------------------------------------------------------- */
typedef struct { char key[MAX_KEY_LEN]; char value[MAX_VAL_LEN]; } KVPair;

static int yaml_is_dangerous(const char *raw) {
    return strstr(raw, "!!") || strstr(raw, "!<") ||
           strchr(raw, '&') || strchr(raw, '*') || strstr(raw, "<<:");
}

static void rtrim_chars(char *s, size_t *len, const char *chars) {
    while (*len > 0 && strchr(chars, s[*len - 1])) (*len)--;
}

/**
 * parse_yaml_safe — parse a minimal safe YAML mapping.
 * Returns number of pairs on success, -1 on unsafe or malformed input.
 */
static int parse_yaml_safe(const char *raw, KVPair *pairs, int max_pairs) {
    if (!raw) return -1;
    size_t rlen = strlen(raw);
    if (rlen == 0 || rlen > MAX_YAML_LEN) return -1;

    /* Explicitly reject unsupported/dangerous constructs */
    if (yaml_is_dangerous(raw)) return -1;

    char buf[MAX_YAML_LEN + 1];
    memcpy(buf, raw, rlen); buf[rlen] = '\0';

    int count = 0;
    char *sv = NULL;
    char *line = strtok_r(buf, "\n", &sv);

    while (line && count < max_pairs) {
        while (*line == ' ' || *line == '\t') line++;
        if (*line == '\0' || *line == '#') { line = strtok_r(NULL, "\n", &sv); continue; }

        char *colon = strchr(line, ':');
        if (!colon) return -1;

        /* Key */
        size_t kl = (size_t)(colon - line);
        while (kl > 0 && (line[kl-1] == ' ' || line[kl-1] == '\t')) kl--;
        if (kl == 0 || kl >= MAX_KEY_LEN) return -1;
        char key[MAX_KEY_LEN] = {0};
        memcpy(key, line, kl);
        for (size_t i = 0; i < kl; i++)
            if (!isalnum((unsigned char)key[i]) && key[i] != '_') return -1;

        /* Value — strip leading/trailing whitespace and quotes */
        char *val = colon + 1;
        while (*val == ' ' || *val == '\t' || *val == '"') val++;
        size_t vl = strlen(val);
        rtrim_chars(val, &vl, " \t\r\"");
        if (vl >= MAX_VAL_LEN) return -1;
        for (size_t i = 0; i < vl; i++) {
            char c = val[i];
            if (!isalnum((unsigned char)c) && c != '_' && c != '-' && c != '.') return -1;
        }

        memcpy(pairs[count].key, key, kl); pairs[count].key[kl] = '\0';
        memcpy(pairs[count].value, val, vl); pairs[count].value[vl] = '\0';
        count++;
        line = strtok_r(NULL, "\n", &sv);
    }
    return count;
}

static const char *kv_get(const KVPair *pairs, int n, const char *key) {
    for (int i = 0; i < n; i++) if (strcmp(pairs[i].key, key) == 0) return pairs[i].value;
    return NULL;
}

/* -------------------------------------------------------------------------
 * Validation helpers
 * ---------------------------------------------------------------------- */

static int validate_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_CODE_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

static long validate_quantity(const char *s) {
    if (!s || !*s) return -1;
    char *end;
    long qty = strtol(s, &end, 10);
    if (end == s || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) return -1;
    return qty;
}

/* -------------------------------------------------------------------------
 * calculate_price — microservice entry point
 *
 * Steps:
 *  1. Parse YAML safely; reject unsupported tags/structures.
 *  2. Validate product_code.
 *  3. Validate quantity.
 *  4. Untrusted input confirmed; safe to call pricing logic.
 *  5. Return total_price as YAML payload.
 * ---------------------------------------------------------------------- */
int calculate_price(const char *raw_yaml, char *out, size_t out_size) {
    if (!out || out_size == 0) return 500;
    if (!raw_yaml || !*raw_yaml) {
        snprintf(out, out_size, "error: invalid request\n"); return 400;
    }

    /* Step 1 — safe parse; rejects unsupported tags/structures */
    KVPair pairs[MAX_PAIRS];
    int n = parse_yaml_safe(raw_yaml, pairs, MAX_PAIRS);
    if (n <= 0) { snprintf(out, out_size, "error: invalid request\n"); return 400; }

    /* Step 2 — validate product_code */
    const char *pc = kv_get(pairs, n, "product_code");
    if (!validate_product_code(pc)) {
        snprintf(out, out_size, "error: invalid product_code\n"); return 400;
    }

    /* Step 3 — validate quantity */
    const char *qs = kv_get(pairs, n, "quantity");
    long qty = validate_quantity(qs);
    if (qty < 0) { snprintf(out, out_size, "error: invalid quantity\n"); return 400; }

    /* Step 4 — all checks passed; safe to call pricing logic */
    double price = get_price_for_product_code(pc);
    if (price < 0) { snprintf(out, out_size, "error: product not found\n"); return 404; }

    /* Step 5 — return total_price as YAML payload */
    snprintf(out, out_size, "total_price: %.2f\n", price * (double)qty);
    return 200;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    load_catalog();
    char out[OUT_LEN];

    struct { const char *yaml; const char *label; } tests[] = {
        { "product_code: WIDGET_A\nquantity: 3\n",                         "valid"          },
        { "product_code: GADGET_X\nquantity: 10\n",                        "valid large"    },
        { "product_code: !!python/object:os.system\nquantity: 1\n",        "tag attack"     },
        { "product_code: WIDGET_A\nquantity: -1\n",                        "neg qty"        },
        { "product_code: WIDGET_A\nquantity: 99999\n",                     "overflow qty"   },
        { "product_code: UNKNOWN_X\nquantity: 1\n",                        "unknown prod"   },
        { "not a mapping\n",                                                "bad structure"  },
        { "",                                                               "empty input"    },
        { NULL, NULL }
    };

    for (int i = 0; tests[i].yaml != NULL; i++) {
        int code = calculate_price(tests[i].yaml, out, sizeof(out));
        printf("[%3d][%-15s] %s", code, tests[i].label, out);
    }
    return 0;
}
