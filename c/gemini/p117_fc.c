#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <fcntl.h>
#include <unistd.h>

// Using /dev/urandom for cryptographically secure random bytes
double get_secure_random_double() {
    unsigned long long random_val;
    int fd = open("/dev/urandom", O_RDONLY);
    read(fd, &random_val, sizeof(random_val));
    close(fd);
    return (double)random_val / (double)0xFFFFFFFFFFFFFFFFULL;
}

int main() {
    double a = get_secure_random_double();
    double b = get_secure_random_double();
    double c = get_secure_random_double();

    char str_a[32], str_b[32], str_c[32];

    // isfinite check ensures safe formatting
    if (isfinite(a)) snprintf(str_a, sizeof(str_a), "%.10f", a);
    if (isfinite(b)) snprintf(str_b, sizeof(str_b), "%.10f", b);
    if (isfinite(c)) snprintf(str_c, sizeof(str_c), "%.10f", c);

    return 0;
}