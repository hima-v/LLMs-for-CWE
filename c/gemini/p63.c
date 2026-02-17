#include <stdio.h>

void process() {
    FILE *in = fopen("source.bin", "rb");
    char buff[512];
    int n = fread(buff, 1, 512, in);
    fclose(in);

    int sum = 0;
    for(int i=0; i<n; i++) sum += buff[i];

    if(sum == 0) printf("Buffer Empty\n");

    FILE *out = fopen("another_file.txt", "w");
    fprintf(out, "%d %d", n, sum);
    fclose(out);
}
