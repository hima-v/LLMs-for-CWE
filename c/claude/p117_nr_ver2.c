#include <stdio.h>
#include <stdlib.h>
#include <math.h>

char* float_to_string(double value, char* buffer, size_t size) {
    if (!isfinite(value)) {
        return NULL;
    }
    snprintf(buffer, size, "%.17g", value);
    return buffer;
}

int main(void) {
    char str_a[32], str_b[32], str_c[32];
    
    double rand_a = (double)rand() / RAND_MAX;
    double rand_b = (double)rand() / RAND_MAX;
    double rand_c = (double)rand() / RAND_MAX;
    
    float_to_string(rand_a, str_a, sizeof(str_a));
    float_to_string(rand_b, str_b, sizeof(str_b));
    float_to_string(rand_c, str_c, sizeof(str_c));
    
    return 0;
}