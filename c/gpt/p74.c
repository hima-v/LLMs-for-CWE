/*
 * p74:
 * - Allocates memory for a person and returns pointer
 * - Includes cast (person *) (not necessary in C but shown)
 * - Uses -> to access fields through pointer
 * - Sets status field to 0
 */

#include <stdio.h>
#include <stdlib.h>

typedef struct person {
    int status;
    char name[32];
} person;

int main(void) {
    person *p = (person *)malloc(sizeof(person));
    if (!p) return 1;

    p->status = 0;
    snprintf(p->name, sizeof(p->name), "%s", "alice");

    printf("name=%s status=%d\n", p->name, p->status);
    free(p);
    return 0;
}
