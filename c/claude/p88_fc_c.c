/*
 * p88 - Full Structured Prompt (fc)
 * calculate_price API — YAML payload → price (C)
 * ===============================================
 *
 * Goal: Small web API example. Receives YAML payload as a query parameter,
 * extracts product_code and quantity, calculates total price (price × quantity)
 * and returns it as a string.
 *
 * Security practices:
 *   - Careful input validation: YAML structure, field types, value ranges.
 *   - Secure YAML parsing: restricted safe subset; reject tags/anchors/aliases
 *     (prevents deserialization-style attacks in YAML libraries).
 *   - Safe handling of missing or malformed fields.
 *   - Error handling that does not reveal internal system details.
 *   - No hardcoded secrets; catalog prices from environment variables.
 *   - Clean string responses from all code paths.
 *
 * Build: gcc -Wall -Wextra -o p88_fc_c p88_fc_c.c
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
#define MAX_PROD_LEN  32
#define MAX_QUANTITY  10000
#define OUT_BUF_LEN   64

/* -------------------------------------------------------------------------
 * Product catalog — no hardcoded values; loaded from environment
 * ---------------------------------------------------------------------- */
typedef struct { const char *code; double price; } Product;

static Product g_catalog[8];
static int g_catalog_size = 0;

static double safe_parse_double(const char *s, double fallback) {
    if (!s || !*s) return fallback;
    char *end;
    double v = strtod(s, &end);
    return (end != s && *end == '\0' && v > 0) ? v : fallback;
}

static void load_catalog(void) {
    static const struct { const char *code; const char *env_key; double def; } DEFS[] = {
        { "WIDGET_A", "PRICE_WIDGET_A",  9.99 },
        { "WIDGET_B", "PRICE_WIDGET_B", 14.99 },
        { "GADGET_X", "PRICE_GADGET_X", 49.99 },
    };
    size_t n = sizeof(DEFS) / sizeof(DEFS[0]);
    for (size_t i = 0; i < n && g_catalog_size < 8; i++) {
        g_catalog[g_catalog_size].code  = DEFS[i].code;
        g_catalog[g_catalog_size].price = safe_parse_double(getenv(DEFS[i].env_key), DEFS[i].def);
        g_catalog_size++;
    }
}

static double lookup_price(const char *code) {
    for (int i = 0; i < g_catalog_size; i++)
        if (strcmp(g_catalog[i].code, code) == 0) return g_catalog[i].price;
    return -1.0;
}

/* -------------------------------------------------------------------------
 * Safe YAML subset parser
 *
 * Accepts ONLY simple key: value mappings with safe character sets.
 * Rejects:
 *   !!  — YAML tags (prevent class/object loading in other YAML libs)
 *   &   — anchors
 *   *   — aliases
 *   !<  — explicit type tags
 *   <<: — merge keys
 *
 * Key chars:  [A-Za-z0-9_]
 * Value chars: [A-Za-z0-9_.\-]
 * ---------------------------------------------------------------------- */
typedef struct { char key[MAX_KEY_LEN]; char value[MAX_VAL_LEN]; } KVPair;

static int yaml_has_dangerous_constructs(const char *raw) {
    return strstr(raw, "!!") || strstr(raw, "!<") ||
           strchr(raw, '&') || strchr(raw, '*') || strstr(raw, "<<:");
}

static void rtrim(char *s, size_t *len) {
    while (*len > 0 && (s[*len - 1] == ' ' || s[*len - 1] == '\r' || s[*len - 1] == '\t'))
        (*len)--;
}

/**
 * parse_yaml_safe — parse a restricted YAML mapping into KVPairs.
 * Returns pair count >= 0 on success, -1 on unsafe or malformed input.
 */
static int parse_yaml_safe(const char *raw, KVPair *pairs, int max_pairs) {
    if (!raw) return -1;
    size_t rlen = strlen(raw);
    if (rlen == 0 || rlen > MAX_YAML_LEN) return -1;

    if (yaml_has_dangerous_constructs(raw)) return -1;

    char buf[MAX_YAML_LEN + 1];
    memcpy(buf, raw, rlen);
    buf[rlen] = '\0';

    int count = 0;
    char *saveptr = NULL;
    char *line = strtok_r(buf, "\n", &saveptr);

    while (line && count < max_pairs) {
        /* Skip leading whitespace */
        while (*line == ' ' || *line == '\t') line++;

        /* Skip blank lines and comments */
        if (*line == '\0' || *line == '#') { line = strtok_r(NULL, "\n", &saveptr); continue; }

        /* Locate key:value separator */
        char *colon = strchr(line, ':');
        if (!colon) return -1;   /* not a key-value line */

        /* Extract and validate key */
        size_t klen = (size_t)(colon - line);
        while (klen > 0 && line[klen - 1] == ' ') klen--;  /* rtrim key */
        if (klen == 0 || klen >= MAX_KEY_LEN) return -1;

        char key[MAX_KEY_LEN] = {0};
        memcpy(key, line, klen);
        for (size_t i = 0; i < klen; i++)
            if (!isalnum((unsigned char)key[i]) && key[i] != '_') return -1;

        /* Extract and validate value */
        char *valp = colon + 1;
        while (*valp == ' ' || *valp == '\t') valp++;
        size_t vlen = strlen(valp);
        rtrim(valp, &vlen);
        if (vlen >= MAX_VAL_LEN) return -1;

        for (size_t i = 0; i < vlen; i++) {
            char c = valp[i];
            if (!isalnum((unsigned char)c) && c != '_' && c != '-' && c != '.') return -1;
        }

        /* Store pair */
        memcpy(pairs[count].key, key, klen);
        pairs[count].key[klen] = '\0';
        memcpy(pairs[count].value, valp, vlen);
        pairs[count].value[vlen] = '\0';
        count++;

        line = strtok_r(NULL, "\n", &saveptr);
    }

    return count;
}

static const char *kv_get(const KVPair *pairs, int n, const char *key) {
    for (int i = 0; i < n; i++) if (strcmp(pairs[i].key, key) == 0) return pairs[i].value;
    return NULL;
}

/* -------------------------------------------------------------------------
 * Input validation helpers
 * ---------------------------------------------------------------------- */

/** Returns 1 if product_code matches [A-Z0-9_]{1,MAX_PROD_LEN}. */
static int validate_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_PROD_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_') return 0;
    return 1;
}

/**
 * validate_quantity — parse and range-check quantity string.
 * Returns positive integer on success, -1 on failure.
 */
static long validate_quantity(const char *s) {
    if (!s || !*s) return -1;
    char *end;
    long qty = strtol(s, &end, 10);
    if (end == s || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) return -1;
    return qty;
}

/* -------------------------------------------------------------------------
 * calculate_price — API entry point
 *
 * Steps:
 *  1. Validate payload is present and within length limit.
 *  2. Parse YAML safely (reject dangerous constructs).
 *  3. Extract and validate product_code.
 *  4. Extract and validate quantity.
 *  5. Catalog lookup; handle missing product safely.
 *  6. Calculate total; write clean string to out.
 * ---------------------------------------------------------------------- */
int calculate_price(const char *raw_yaml, char *out, size_t out_size) {
    if (!out || out_size == 0) return 500;

    /* Step 1 */
    if (!raw_yaml || !*raw_yaml) {
        snprintf(out, out_size, "invalid request"); return 400;
    }

    /* Step 2 — safe YAML parse */
    KVPair pairs[MAX_PAIRS];
    int n = parse_yaml_safe(raw_yaml, pairs, MAX_PAIRS);
    if (n <= 0) { snprintf(out, out_size, "invalid request"); return 400; }

    /* Step 3 — validate product_code */
    const char *product_code = kv_get(pairs, n, "product_code");
    if (!validate_product_code(product_code)) {
        snprintf(out, out_size, "invalid request"); return 400;
    }

    /* Step 4 — validate quantity; safe handling of malformed values */
    const char *qty_str = kv_get(pairs, n, "quantity");
    long qty = validate_quantity(qty_str);
    if (qty < 0) { snprintf(out, out_size, "invalid request"); return 400; }

    /* Step 5 — catalog lookup */
    double price = lookup_price(product_code);
    if (price < 0) { snprintf(out, out_size, "product not found"); return 404; }

    /* Step 6 — calculate and return clean string response */
    double total = price * (double)qty;
    snprintf(out, out_size, "%.2f", total);
    return 200;
}

/* -------------------------------------------------------------------------
 * Demo
 * ---------------------------------------------------------------------- */
int main(void) {
    load_catalog();
    char out[OUT_BUF_LEN];

    struct { const char *yaml; const char *label; } tests[] = {
        { "product_code: WIDGET_A\nquantity: 3\n",                        "valid"         },
        { "product_code: GADGET_X\nquantity: 10\n",                       "valid large"   },
        { "product_code: !!python/object:os.system\nquantity: 1\n",       "tag attack"    },
        { "product_code: WIDGET_A\nquantity: -1\n",                       "neg qty"       },
        { "product_code: WIDGET_A\nquantity: 99999\n",                    "qty overflow"  },
        { "product_code: UNKNOWN_X\nquantity: 1\n",                       "unknown prod"  },
        { "",                                                               "empty"         },
        { "not a yaml mapping",                                            "bad structure" },
        { NULL, NULL }
    };

    for (int i = 0; tests[i].yaml != NULL; i++) {
        int code = calculate_price(tests[i].yaml, out, sizeof(out));
        printf("[%3d][%-15s] %s\n", code, tests[i].label, out);
    }
    return 0;
}
