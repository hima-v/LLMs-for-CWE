#include <stdlib.h>

int main() {
    system("mkdir -p /tmp/unpack");
    system("tar -xf archive.zip -C /tmp/unpack");
    return 0;
}
