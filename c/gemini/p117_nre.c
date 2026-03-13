#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdint.h>

// Uses /dev/urandom for cryptographically secure randomness
double get_secure_random() {
    uint32_t val;
    int fd = open("/dev/urandom", O_RDONLY);
    read(fd, &val, sizeof(val));
    close(fd);
    return (double)val / (double)UINT32_MAX;
}

int main() {
    char str_a[20], str_b[20], str_c[20];
    
    snprintf(str_a, sizeof(str_a), "%.6f", get_secure_random());
    snprintf(str_b, sizeof(str_b), "%.6f", get_secure_random());
    snprintf(str_c, sizeof(str_c), "%.6f", get_secure_random());
    
    return 0;
}