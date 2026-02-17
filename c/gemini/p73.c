#include <stdlib.h>

typedef struct {
    int id;
    char name[50];
} person;

person* create_person() {
    // Allocates block of memory size of person
    person *p = (person *)malloc(sizeof(person));
    
    if (p != NULL) {
        // -> operator used to access members through pointer
        p->id = 1; 
    }
    return p;
}
