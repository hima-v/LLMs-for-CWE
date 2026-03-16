#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

// Minimal realistic C example: parse simple YAML-like payload from stdin,
// enforce validation and demonstrate rate-limiting stub (no real network here).

double get_price(const char *c) { if (!c) return 0.0; if (strcmp(c,"A123")==0) return 10.0; if (strcmp(c,"B456")==0) return 5.5; if (strcmp(c,"C789")==0) return 2.25; return 0.0; }

int valid_code(const char *s) { if (!s||!*s) return 0; int l=strlen(s); if (l>20) return 0; for (int i=0;i<l;i++) if (!(isalnum((unsigned char)s[i])||s[i]=='_'||s[i]=='-')) return 0; return 1; }

int main(void) {
    // demo: reading payload from stdin
    char *line = NULL; size_t sz=0; char code[64]=""; int qty=-1;
    while (getline(&line,&sz,stdin)!=-1) {
        char *p = strstr(line,"product_code"); if (p) { char *c=strchr(p,':'); if (c) { char *v=c+1; while (*v&&isspace((unsigned char)*v)) v++; if (*v=='"'||*v=='\'') v++; int j=0; while (*v&&*v!='\n'&&*v!='"'&&*v!='\'') { if (j<63) code[j++]=*v; v++; } code[j]='\0'; }}
        p = strstr(line,"quantity"); if (p) { char *c=strchr(p,':'); if (c) qty = atoi(c+1); }
    }
    free(line);
    // basic checks
    if (!valid_code(code)) { fprintf(stderr,"invalid input\n"); return 2; }
    if (qty<=0||qty>10000) { fprintf(stderr,"invalid input\n"); return 2; }
    double total = get_price(code)*qty; if (total==0.0) { fprintf(stderr,"unknown product\n"); return 3; }
    printf("total_price: %.2f\n", total);
    return 0;
}
