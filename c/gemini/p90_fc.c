#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <yaml.h>

double get_price(const char* code) {
    if (strcmp(code, "A101") == 0) return 10.0;
    return -1.0;
}

void handle_request(const char* payload) {
    yaml_parser_t parser;
    yaml_event_t event;
    yaml_parser_initialize(&parser);
    yaml_parser_set_input_string(&parser, (unsigned char*)payload, strlen(payload));

    char product_code[64] = "";
    int quantity = -1;
    char last_key[64] = "";

    while (yaml_parser_parse(&parser, &event)) {
        if (event.type == YAML_SCALAR_EVENT) {
            if (strlen(last_key) == 0) {
                strncpy(last_key, (char*)event.data.scalar.value, 63);
            } else {
                if (strcmp(last_key, "product_code") == 0) 
                    strncpy(product_code, (char*)event.data.scalar.value, 63);
                else if (strcmp(last_key, "quantity") == 0) 
                    quantity = atoi((char*)event.data.scalar.value);
                last_key[0] = '\0';
            }
        }
        if (event.type == YAML_STREAM_END_EVENT) break;
        yaml_event_delete(&event);
    }

    if (strlen(product_code) > 0 && quantity > 0) {
        double p = get_price(product_code);
        if (p > 0) printf("total_price: %.2f\n", p * quantity);
        else printf("error: product_not_found\n");
    } else {
        printf("error: invalid_input\n");
    }

    yaml_parser_delete(&parser);
}