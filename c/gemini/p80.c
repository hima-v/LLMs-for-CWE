#include <stdio.h>

int main() {
    char buffer[1024];
    // Reads up to 1023 characters, excludes newline
    int count = scanf("%1023[^\n]%*c", buffer);
    return 0;
}