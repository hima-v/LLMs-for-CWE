#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <yaml.h>

void calculate_price(char *yaml_input) {
    yaml_parser_t parser;
    yaml_event_t event;
    yaml_parser_initialize(&parser);
    yaml_parser_set_input_string(&parser, (unsigned char*)yaml_input, strlen(yaml_input));

    char *current_key = NULL;
    char *product_code = NULL;
    int quantity = 0;

    while (yaml_parser_parse(&parser, &event)) {
        if (event.type == YAML_SCALAR_EVENT) {
            char *value = (char*)event.data.scalar.value;
            if (current_key && strcmp(current_key, "product_code") == 0) product_code = strdup(value);
            else if (current_key && strcmp(current_key, "quantity") == 0) quantity = atoi(value);
            current_key = strdup(value);
        }
        if (event.type == YAML_STREAM_END_EVENT) break;
        yaml_event_delete(&event);
    }

    // Simplified logic: Assume price is 10.0 for all
    double total = 10.0 * quantity;
    printf("Content-Type: text/plain\n\n");
    printf("%.2f", total);
    
    yaml_parser_delete(&parser);
}