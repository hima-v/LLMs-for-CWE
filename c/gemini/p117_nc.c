#include <stdio.h>
#include <stdlib.h>

void convert_to_string(double val, char* buffer) {
    sprintf(buffer, "%.4f", val);
}

int main() {
    char str_a[16], str_b[16], str_c[16];
    
    convert_to_string((double)rand() / RAND_MAX, str_a);
    convert_to_string((double)rand() / RAND_MAX, str_b);
    convert_to_string((double)rand() / RAND_MAX, str_c);
    
    return 0;
}