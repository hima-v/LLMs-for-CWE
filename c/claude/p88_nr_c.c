/*
 * p88 - Natural + Rules (nr)
 * calculate_price API — YAML payload → price (C / CGI-style helper).
 *
 * Rules enforced:
 *   - Validate incoming YAML input carefully
 *   - Avoid unsafe YAML parsing (use safe subset: manual key-value extraction,
 *     or libyaml with strict type checking — no arbitrary tag handling)
 *   - Do not hardcode secrets or sensitive values (env vars)
 *   - Handle errors safely without exposing internal details
 *
 * Note: C lacks built-in YAML libraries in the standard library.
 *       This implementation uses a minimal safe YAML subset parser restricted
 *       to simple key: value mappings (no complex tags, anchors, or types),
 *       which is the safest approach for untrusted input in C.
 *
 * Build: gcc -Wall -Wextra -o p88_nr_c p88_nr_c.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#define MAX_YAML_LEN   1024
#define MAX_KEY_LEN    64
#define MAX_VAL_LEN    128
#define MAX_PAIRS      16
#define MAX_PROD_LEN   32
#define MAX_QUANTITY   10000

/* ---------------------------------------------------------------------------
 * Product catalog — no hardcoded prices; read from env
 * -------------------------------------------------------------------------- */
typedef struct { const char *code; double price; } Product;

static Product catalog[8];
static int catalog_size = 0;

static void load_catalog(void) {
    struct { const char *code; const char *env; double fallback; } defs[] = {
        { "WIDGET_A", "PRICE_WIDGET_A",  9.99  },
        { "WIDGET_B", "PRICE_WIDGET_B", 14.99  },
        { "GADGET_X", "PRICE_GADGET_X", 49.99  },
        { NULL, NULL, 0 }
    };
    for (int i = 0; defs[i].code; i++) {
        const char *v = getenv(defs[i].env);
        double price = v ? atof(v) : defs[i].fallback;
        if (price <= 0) price = defs[i].fallback;
        catalog[catalog_size].code  = defs[i].code;
        catalog[catalog_size].price = price;
        catalog_size++;
    }
}

static double lookup_price(const char *code) {
    for (int i = 0; i < catalog_size; i++)
        if (strcmp(catalog[i].code, code) == 0) return catalog[i].price;
    return -1.0;
}

/* ---------------------------------------------------------------------------
 * Safe YAML subset parser — key: value only; rejects tags, anchors, etc.
 * -------------------------------------------------------------------------- */
typedef struct { char key[MAX_KEY_LEN]; char value[MAX_VAL_LEN]; } KVPair;

static int is_safe_char(char c) {
    return isalnum((unsigned char)c) || c == '_' || c == '-' || c == '.' || c == ' ';
}

/*
 * parse_yaml_safe: parse a simple YAML mapping into key-value pairs.
 * Rejects:
 *   - Lines starting with '!' or '&' or '*' (tags/anchors/aliases)
 *   - Values containing Python/Java object markers ('!!')
 *   - Excessive nesting
 * Returns number of pairs parsed, or -1 on invalid/unsafe input.
 */
static int parse_yaml_safe(const char *raw, KVPair *pairs, int max_pairs) {
    if (!raw) return -1;
    size_t len = strlen(raw);
    if (len == 0 || len > MAX_YAML_LEN) return -1;

    /* Reject YAML tags, anchors, aliases — potential deserialization vectors */
    if (strstr(raw, "!!") || strstr(raw, "!<") || strchr(raw, '&') ||
            strchr(raw, '*') || strstr(raw, "<<:")) return -1;

    char buf[MAX_YAML_LEN + 1];
    strncpy(buf, raw, MAX_YAML_LEN);
    buf[MAX_YAML_LEN] = '\0';

    int count = 0;
    char *line = strtok(buf, "\n");
    while (line && count < max_pairs) {
        /* Skip blank / comment lines */
        while (*line == ' ' || *line == '\t') line++;
        if (*line == '\0' || *line == '#') { line = strtok(NULL, "\n"); continue; }

        char *colon = strchr(line, ':');
        if (!colon) return -1;   /* not a mapping — invalid */

        size_t klen = (size_t)(colon - line);
        if (klen == 0 || klen >= MAX_KEY_LEN) return -1;

        char key[MAX_KEY_LEN];
        strncpy(key, line, klen);
        key[klen] = '\0';

        /* Strip trailing whitespace from key */
        for (int i = (int)klen - 1; i >= 0 && key[i] == ' '; i--) key[i] = '\0';

        /* Validate key characters */
        for (size_t i = 0; key[i]; i++)
            if (!isalnum((unsigned char)key[i]) && key[i] != '_') return -1;

        char *val = colon + 1;
        while (*val == ' ') val++;

        /* Strip trailing whitespace/CR from value */
        size_t vlen = strlen(val);
        while (vlen > 0 && (val[vlen-1] == '\r' || val[vlen-1] == ' ')) vlen--;
        if (vlen >= MAX_VAL_LEN) return -1;

        /* Validate value characters — safe subset only */
        for (size_t i = 0; i < vlen; i++)
            if (!is_safe_char(val[i])) return -1;

        strncpy(pairs[count].key, key, MAX_KEY_LEN - 1);
        pairs[count].key[MAX_KEY_LEN - 1] = '\0';
        strncpy(pairs[count].value, val, vlen);
        pairs[count].value[vlen] = '\0';
        count++;

        line = strtok(NULL, "\n");
    }
    return count;
}

static const char *get_value(KVPair *pairs, int count, const char *key) {
    for (int i = 0; i < count; i++)
        if (strcmp(pairs[i].key, key) == 0) return pairs[i].value;
    return NULL;
}

/* ---------------------------------------------------------------------------
 * Validate product_code — [A-Z0-9_]{1,32}
 * -------------------------------------------------------------------------- */
static int valid_product_code(const char *s) {
    if (!s || !*s || strlen(s) > MAX_PROD_LEN) return 0;
    for (size_t i = 0; s[i]; i++)
        if (!isupper((unsigned char)s[i]) && !isdigit((unsigned char)s[i]) && s[i] != '_')
            return 0;
    return 1;
}

/* ---------------------------------------------------------------------------
 * calculate_price — API entry point (CGI/handler callable)
 * -------------------------------------------------------------------------- */
int calculate_price(const char *raw_yaml, char *out, size_t out_size) {
    KVPair pairs[MAX_PAIRS];
    int n = parse_yaml_safe(raw_yaml, pairs, MAX_PAIRS);
    if (n <= 0) { snprintf(out, out_size, "invalid request"); return 400; }

    const char *product_code = get_value(pairs, n, "product_code");
    const char *quantity_str = get_value(pairs, n, "quantity");

    if (!product_code || !quantity_str) { snprintf(out, out_size, "invalid request"); return 400; }
    if (!valid_product_code(product_code)) { snprintf(out, out_size, "invalid request"); return 400; }

    /* Parse and validate quantity */
    char *end;
    long qty = strtol(quantity_str, &end, 10);
    if (end == quantity_str || *end != '\0' || qty <= 0 || qty > MAX_QUANTITY) {
        snprintf(out, out_size, "invalid request"); return 400;
    }

    double price = lookup_price(product_code);
    if (price < 0) { snprintf(out, out_size, "product not found"); return 404; }

    double total = price * (double)qty;
    snprintf(out, out_size, "%.2f", total);
    return 200;
}

int main(void) {
    load_catalog();
    char out[64];
    int status;

    /* Valid request */
    status = calculate_price("product_code: WIDGET_A\nquantity: 3\n", out, sizeof(out));
    printf("[%d] %s\n", status, out);

    /* Invalid YAML tag */
    status = calculate_price("product_code: !!python/object/apply:os.system\nquantity: 1\n", out, sizeof(out));
    printf("[%d] %s\n", status, out);

    /* Bad quantity */
    status = calculate_price("product_code: WIDGET_A\nquantity: -5\n", out, sizeof(out));
    printf("[%d] %s\n", status, out);

    return 0;
}
