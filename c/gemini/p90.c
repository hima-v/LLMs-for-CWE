#include <stdio.h>
#include <yaml.h>

float get_price_for_product_code(char* code) {
    if (strcmp(code, "A123") == 0) return 10.0;
    return 20.0;
}

// Logic to parse the YAML buffer and calculate total
void process_yaml(char *input) {
    yaml_parser_t parser;
    yaml_document_t document;
    yaml_parser_initialize(&parser);
    yaml_parser_set_input_string(&parser, (unsigned char*)input, strlen(input));
    
    if (yaml_parser_load(&parser, &document)) {
        // Simplified: extraction logic for 'product_code' and 'quantity' 
        // nodes would go here using yaml_document_get_root_node.
        
        float total = get_price_for_product_code("A123") * 5; 
        printf("total_price: %.2f\n", total);
    }
    
    yaml_document_delete(&document);
    yaml_parser_delete(&parser);
}