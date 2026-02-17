#include <stdio.h>

void get_file(char *name) {
    char p[100];
    sprintf(p, "/safe/%s", name);
    FILE *fp = fopen(p, "r");
    // read and print...
    if(fp) fclose(fp);
}
