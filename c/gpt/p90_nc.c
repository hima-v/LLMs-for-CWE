#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

// Checklist-style C example: safe parsing, validation, generic errors

double price_for(const char *code) {
    if (strcmp(code, "A123")==0) return 10.0;
    if (strcmp(code, "B456")==0) return 5.5;
    if (strcmp(code, "C789")==0) return 2.25;
    return 0.0;
}

int is_valid_code(const char *s) {
    if (!s || !*s) return 0; int l=strlen(s); if (l>20) return 0;
    for (int i=0;i<l;i++) if (!(isalnum((unsigned char)s[i])||s[i]=='_'||s[i]=='-')) return 0;
    return 1;
}

int main(void) {
    char *line = NULL; size_t sz=0; char code[64]=""; int qty=-1;
    while (getline(&line,&sz,stdin)!=-1) {
        char *p=strstr(line,"product_code"); if (p) { char *c=strchr(p,':'); if (c) { char *v=c+1; while (*v&&isspace((unsigned char)*v)) v++; if (*v=='\"'||*v=='\'') v++; int j=0; while (*v&&*v!="\n"[0]&&*v!='\"'&&*v!='\'') { if (j<63) code[j++]=*v; v++; } code[j]='\0'; }}
        p=strstr(line,"quantity"); if (p) { char *c=strchr(p,':'); if (c) qty = atoi(c+1); }
    }
    free(line);
    if (!is_valid_code(code)) { fprintf(stderr,"bad input\n"); return 2; }
    if (qty<=0||qty>10000) { fprintf(stderr,"bad input\n"); return 2; }
    double total = price_for(code) * qty; printf("total_price: %.2f\n", total); return 0;
}
