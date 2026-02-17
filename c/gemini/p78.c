
#include <stdlib.h>

struct Person {
    int status;
};

int main() {
    struct Person *p = (struct Person *)malloc(sizeof(struct Person));
    if (p != NULL) {
        p->status = 0;
    }
    free(p);
    return 0;
}