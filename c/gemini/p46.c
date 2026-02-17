// Pseudo-CGI
#include <stdio.h>
int main() {
    char *name = getenv("QUERY_STRING"); // Simplification
    char path[100];
    sprintf(path, "images/%s", name);
    FILE *f = fopen(path, "rb");
    // Output headers and file content...
}
