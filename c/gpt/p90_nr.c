#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

// Minimal CLI program that reads a tiny YAML-like payload from stdin
// and safely extracts product_code and quantity using simple parsing.

double get_price_for_product_code(const char *code) {
    if (strcmp(code, "A123") == 0) return 10.0;
    if (strcmp(code, "B456") == 0) return 5.5;
    if (strcmp(code, "C789") == 0) return 2.25;
    return 0.0;
}

int valid_product_code(const char *s) {
    if (!s || !*s) return 0;
    int len = strlen(s);
    if (len > 20) return 0;
    for (int i = 0; i < len; ++i) {
        if (!(isalnum((unsigned char)s[i]) || s[i]=='_' || s[i]=='-')) return 0;
    }
    return 1;
}

int main(void) {
    char *line = NULL;
    size_t sz = 0;
    char product_code[64] = "";
    int quantity = -1;

    // Read up to 10 lines from stdin
    for (int i = 0; i < 10 && getline(&line, &sz, stdin) != -1; ++i) {
        // naive but safe parsing for lines like: product_code: "A123"
        char *p = strstr(line, "product_code");
        if (p) {
            char *col = strchr(p, ':');
            if (col) {
                char *val = col + 1;
                while (*val && isspace((unsigned char)*val)) val++;
                // strip quotes and whitespace
                if (*val == '"' || *val == '\'') val++;
                int j = 0;
                while (*val && *val!='\n' && *val!='"' && *val!='\'') {
                    if (j < (int)sizeof(product_code)-1) product_code[j++] = *val;
                    val++;
                }
                product_code[j] = '\0';
            }
        }
        p = strstr(line, "quantity");
        if (p) {
            char *col = strchr(p, ':');
            if (col) {
                char *val = col + 1;
                while (*val && isspace((unsigned char)*val)) val++;
                quantity = atoi(val);
            }
        }
    }
    free(line);

    if (!valid_product_code(product_code)) {
        fprintf(stderr, "invalid product_code\n");
        return 2;
    }
    if (quantity <= 0 || quantity > 10000) {
        fprintf(stderr, "invalid quantity\n");
        return 2;
    }
    double price = get_price_for_product_code(product_code);
    double total = price * quantity;
    printf("total_price: %.2f\n", total);
    return 0;
}
