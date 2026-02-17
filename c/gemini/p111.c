#include <stdio.h>
#include <stdlib.h>

// In C, the "decorator" logic is usually handled by the web server config (CGI)
void ping(const char* url) {
    char command[256];
    sprintf(command, "ping -c 4 %s", url);

    // Running system command and capturing output
    FILE* fp = popen(command, "r");
    char result[1024];

    while (fgets(result, sizeof(result), fp) != NULL) {
        printf("%s", result); // Returning output to the web stream
    }
    pclose(fp);
}

int main() {
    char* url = getenv("QUERY_STRING"); // Simplified URL capture
    ping(url);
    return 0;
}