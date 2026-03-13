#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdint.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

static int ensure_dir(const char *dir) {
    if (mkdir(dir, 0700) == 0) return 0;
    if (errno == EEXIST) return 0;
    return -1;
}

static int safe_filename(const char *name) {
    if (!name || !*name) return 0;
    if (strchr(name, '/') || strchr(name, '\\')) return 0;
    if (strstr(name, "..")) return 0;
    return 1;
}

static int read_exact(int fd, void *buf, size_t len) {
    size_t off = 0;
    while (off < len) {
        ssize_t n = read(fd, (unsigned char*)buf + off, len - off);
        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        if (n == 0) return -1;
        off += (size_t)n;
    }
    return 0;
}

static int write_all(int fd, const void *buf, size_t len) {
    size_t off = 0;
    while (off < len) {
        ssize_t n = write(fd, (const unsigned char*)buf + off, len - off);
        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        off += (size_t)n;
    }
    return 0;
}

static uint64_t urand_u64(void) {
    uint64_t x = 0;
    int fd = open("/dev/urandom", O_RDONLY | O_CLOEXEC);
    if (fd < 0) exit(1);
    if (read_exact(fd, &x, sizeof(x)) != 0) {
        close(fd);
        exit(1);
    }
    close(fd);
    return x;
}

static double secure_float01(void) {
    uint64_t r = urand_u64();
    uint64_t mant = r >> 11; /* 53 bits */
    return (double)mant / (double)(1ULL << 53);
}

int main(void) {
    const char *base = "./output";
    const char *file = "random_floats.txt";

    if (ensure_dir(base) != 0) return 1;
    if (!safe_filename(file)) return 1;

    char path[512];
    int n = snprintf(path, sizeof(path), "%s/%s", base, file);
    if (n < 0 || (size_t)n >= sizeof(path)) return 1;

    double a = secure_float01();
    double b = secure_float01();
    double c = secure_float01();

    char sa[64], sb[64], sc[64];
    if (snprintf(sa, sizeof(sa), "%.17g", a) < 0) return 1;
    if (snprintf(sb, sizeof(sb), "%.17g", b) < 0) return 1;
    if (snprintf(sc, sizeof(sc), "%.17g", c) < 0) return 1;

    size_t la = strlen(sa), lb = strlen(sb), lc = strlen(sc);
    size_t total = la + lb + lc;

    char *out = (char*)malloc(total + 1);
    if (!out) return 1;

    memcpy(out, sa, la);
    memcpy(out + la, sb, lb);
    memcpy(out + la + lb, sc, lc);
    out[total] = '\0';

    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC | O_CLOEXEC, 0600);
    if (fd < 0) {
        free(out);
        return 1;
    }
    (void)fchmod(fd, 0600);

    int ok = write_all(fd, out, total);
    close(fd);
    free(out);

    return ok == 0 ? 0 : 1;
}