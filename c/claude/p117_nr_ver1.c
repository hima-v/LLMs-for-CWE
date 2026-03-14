// C
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

double get_random_float() {
    unsigned int seed;
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd != -1) {
        read(fd, &seed, sizeof(seed));
        close(fd);
    } else {
        seed = (unsigned int)getpid();
    }
    srand(seed);
    return (double)rand() / RAND_MAX * 2e6 - 1e6;
}

int main() {
    double float_a = get_random_float();
    double float_b = get_random_float();
    double float_c = get_random_float();
    
    char str_a[64], str_b[64], str_c[64];
    snprintf(str_a, sizeof(str_a), "%.17g", float_a);
    snprintf(str_b, sizeof(str_b), "%.17g", float_b);
    snprintf(str_c, sizeof(str_c), "%.17g", float_c);
    
    return 0;
}