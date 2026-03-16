#include <stdio.h>
#include <string.h>
#include <yaml.h>

// SECURE: Constants instead of hardcoded logic
#define APPLE_PRICE 0.5
#define BANANA_PRICE 0.3

void calculate_price(const char *input_yaml) {
    yaml_parser_t parser;
    yaml_event_t event;
    
    char product_code[64] = {0};
    double quantity = 0;
    char *current_key = NULL;

    yaml_parser_initialize(&parser);
    yaml_parser_set_input_string(&parser, (unsigned char*)input_yaml, strlen(input_yaml));

    while (yaml_parser_parse(&parser, &event)) {
        if (event.type == YAML_SCALAR_EVENT) {
            char *value = (char *)event.data.scalar.value;
            if (current_key == NULL) {
                current_key = strdup(value);
            } else {
                if (strcmp(current_key, "product_code") == 0) {
                    strncpy(product_code, value, sizeof(product_code)-1);
                } else if (strcmp(current_key, "quantity") == 0) {
                    quantity = atof(value);
                }
                free(current_key);
                current_key = NULL;
            }
        }
        if (event.type == YAML_STREAM_END_EVENT) break;
        yaml_event_delete(&event);
    }

    // Calculation and Validation
    if (quantity > 0 && strlen(product_code) > 0) {
        double price = 0;
        if (strcmp(product_code, "apple") == 0) price = APPLE_PRICE;
        else if (strcmp(product_code, "banana") == 0) price = BANANA_PRICE;

        if (price > 0) printf("Total: %.2f\n", price * quantity);
        else printf("Error: Product not found\n");
    } else {
        printf("Error: Invalid Input\n");
    }

    yaml_parser_delete(&parser);
}