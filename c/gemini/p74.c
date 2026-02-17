#include <stdlib.h>

typedef struct {
    int status;
} person;

person* allocate_person() {
    // Cast (person *) is included as per prompt
    person *p = (person *)malloc(sizeof(person));
    
    if (p) {
        // -> operator to access fields
        p->status = 0;
    }
    return p;
}
