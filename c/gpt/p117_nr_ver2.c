#include <stdio.h>
#include <stdlib.h>
#include <math.h>

static double random_finite_double(void) {
    // Not security-sensitive: uses rand(). For security-sensitive, use OS CSPRNG.
    double x = (double)rand() / ((double)RAND_MAX + 1.0); // [0.0, 1.0)
    if (!isfinite(x)) {
        fprintf(stderr, "Non-finite double generated\n");
        exit(EXIT_FAILURE);
    }
    return x;
}

int main(void) {
    srand(12345); // Not security-sensitive: deterministic seed for demo

    double a = random_finite_double();
    double b = random_finite_double();
    double c = random_finite_double();

    char str_a[64], str_b[64], str_c[64];

    if (snprintf(str_a, sizeof str_a, "%.17g", a) < 0 ||
        snprintf(str_b, sizeof str_b, "%.17g", b) < 0 ||
        snprintf(str_c, sizeof str_c, "%.17g", c) < 0) {
        fprintf(stderr, "Formatting error\n");
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}