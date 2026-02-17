#include <stdlib.h>

typedef struct {
    int status;
} person;

person* init_person() {
    person *ptr = malloc(sizeof(person));
    if (ptr != NULL) {
        ptr->status = 0;
    }
    return ptr;
}
