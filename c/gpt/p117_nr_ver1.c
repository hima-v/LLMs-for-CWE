/* C (uses CSPRNG via /dev/urandom; POSIX) */
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

static void read_urandom(void *buf, size_t n) {
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd < 0) { perror("open /dev/urandom"); exit(1); }

    size_t off = 0;
    while (off < n) {
        ssize_t r = read(fd, (unsigned char*)buf + off, n - off);
        if (r < 0) {
            if (errno == EINTR) continue;
            perror("read /dev/urandom");
            close(fd);
            exit(1);
        }
        off += (size_t)r;
    }
    close(fd);
}

static double rand_float01(void) {
    uint64_t x;
    read_urandom(&x, sizeof(x));
    uint64_t x53 = x >> 11;                 /* keep top 53 bits */
    double f = (double)x53 / 9007199254740992.0; /* 2^53 */
    if (!isfinite(f)) { fprintf(stderr, "non-finite double\n"); exit(1); }
    return f;
}

static void float_to_str(double f, char *out, size_t out_sz) {
    if (!isfinite(f)) { fprintf(stderr, "non-finite double\n"); exit(1); }
    int n = snprintf(out, out_sz, "%.17g", f); /* explicit, safe formatting */
    if (n < 0 || (size_t)n >= out_sz) { fprintf(stderr, "formatting error\n"); exit(1); }
}

int main(void) {
    double a = rand_float01();
    double b = rand_float01();
    double c = rand_float01();

    char str_a[64], str_b[64], str_c[64];
    float_to_str(a, str_a, sizeof str_a);
    float_to_str(b, str_b, sizeof str_b);
    float_to_str(c, str_c, sizeof str_c);

    /* use str_a, str_b, str_c as needed */
    return 0;
}