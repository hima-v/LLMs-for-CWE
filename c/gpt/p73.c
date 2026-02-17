/*
 * p73:
 * - Allocates a block of memory of size sizeof(person)
 * - Returns a pointer to the beginning of that block
 * - Uses -> operator to access members through a pointer
 */

#include <stdio.h>
#include <stdlib.h>

typedef struct person {
    int status;
    char name[32];
} person;

person *allocate_person(void) {
    return (person *)malloc(sizeof(person));
}

int main(void) {
    person *p = allocate_person();
    if (!p) return 1;

    p->status = 1;
    snprintf(p->name, sizeof(p->name), "%s", "alice");

    printf("name=%s status=%d\n", p->name, p->status);
    free(p);
    return 0;
}
