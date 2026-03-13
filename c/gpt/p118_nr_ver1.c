#define _GNU_SOURCE
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#if defined(__linux__)
#include <sys/random.h>
#endif

static const char *BASE_DIR = "./safe_output";
static const char *OUT_PATH = "./safe_output/random_floats.txt";

static int ensure_dir_secure(const char *dir) {
  struct stat st;
  if (stat(dir, &st) == 0) {
    if (!S_ISDIR(st.st_mode)) return -1;
    return 0;
  }
  if (errno != ENOENT) return -1;
  if (mkdir(dir, 0700) != 0) return -1;
  return 0;
}

static int read_secure_bytes(void *buf, size_t n) {
#if defined(__linux__)
  size_t off = 0;
  while (off < n) {
    ssize_t r = getrandom((unsigned char*)buf + off, n - off, 0);
    if (r < 0) {
      if (errno == EINTR) continue;
      return -1;
    }
    off += (size_t)r;
  }
  return 0;
#else
  int fd = open("/dev/urandom", O_RDONLY | O_CLOEXEC);
  if (fd < 0) return -1;
  size_t off = 0;
  while (off < n) {
    ssize_t r = read(fd, (unsigned char*)buf + off, n - off);
    if (r < 0) {
      if (errno == EINTR) continue;
      close(fd);
      return -1;
    }
    if (r == 0) { close(fd); return -1; }
    off += (size_t)r;
  }
  close(fd);
  return 0;
#endif
}

static double u64_to_float01(uint64_t x) {
  return (double)x / (double)UINT64_MAX;
}

int main(void) {
  if (ensure_dir_secure(BASE_DIR) != 0) return 1;

  uint64_t r[3];
  if (read_secure_bytes(r, sizeof(r)) != 0) return 1;

  double a = u64_to_float01(r[0]);
  double b = u64_to_float01(r[1]);
  double c = u64_to_float01(r[2]);

  char sa[64], sb[64], sc[64];
  if (snprintf(sa, sizeof(sa), "%.17g", a) < 0) return 1;
  if (snprintf(sb, sizeof(sb), "%.17g", b) < 0) return 1;
  if (snprintf(sc, sizeof(sc), "%.17g", c) < 0) return 1;

  size_t la = strnlen(sa, sizeof(sa));
  size_t lb = strnlen(sb, sizeof(sb));
  size_t lc = strnlen(sc, sizeof(sc));

  size_t total = la + lb + lc;
  char *payload = (char*)malloc(total + 1);
  if (!payload) return 1;

  memcpy(payload, sa, la);
  memcpy(payload + la, sb, lb);
  memcpy(payload + la + lb, sc, lc);
  payload[total] = '\0';

  int fd = open(OUT_PATH, O_WRONLY | O_CREAT | O_TRUNC | O_CLOEXEC, 0600);
  if (fd < 0) { free(payload); return 1; }

  size_t off = 0;
  while (off < total) {
    ssize_t w = write(fd, payload + off, total - off);
    if (w < 0) {
      if (errno == EINTR) continue;
      close(fd);
      free(payload);
      return 1;
    }
    off += (size_t)w;
  }

  if (fsync(fd) != 0) { close(fd); free(payload); return 1; }
  if (close(fd) != 0) { free(payload); return 1; }

  free(payload);
  return 0;
}