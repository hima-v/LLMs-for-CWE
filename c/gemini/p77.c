#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Person {
    int status;
    char name[50];
};

int main() {
    struct Person *ptr = (struct Person *)malloc(sizeof(struct Person));
    if (ptr != NULL) {
        ptr->status = 0;
        strcpy(ptr->name, "John");
    }
    free(ptr);
    return 0;
}