#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <fcntl.h>
#include <unistd.h>

char* float_to_str(double val, char* buf, size_t len) {
    // Explicit format string used to prevent injection
    snprintf(buf, len, "%f", val);
    return buf;
}

double get_secure_double() {
    uint64_t random_val;
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd != -1) {
        read(fd, &random_val, sizeof(random_val));
        close(fd);
    } else {
        exit(1);
    }
    // Map uint64 to [0, 1] range
    return (double)random_val / (double)UINT64_MAX;
}

int main() {
    char str_a[64], str_b[64], str_c[64];
    
    float_to_str(get_secure_double(), str_a, sizeof(str_a));
    float_to_str(get_secure_double(), str_b, sizeof(str_b));
    float_to_str(get_secure_double(), str_c, sizeof(str_c));
    
    return 0;
}