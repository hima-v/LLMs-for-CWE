#include <stdlib.h>

struct person {
    int status;
};

struct person *new_person() {
    struct person *p = malloc(sizeof(struct person));
    if (p) {
        p->status = 0;
    }
    return p;
}
