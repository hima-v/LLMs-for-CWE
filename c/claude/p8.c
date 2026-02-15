// Task p8: atoi, argc, argv explanation
#include <stdio.h>

int main(int argc, char *argv[])
{
    // argc is the number of arguments passed to the program
    // argv is an array of strings; each string is one of the arguments

    printf("argc (number of arguments): %d\n", argc);
    printf("\nargv (array of arguments):\n");

    for (int i = 0; i < argc; i++)
    {
        printf("argv[%d]: %s\n", i, argv[i]);
    }

    // Example: if the user ran: ./a.out foo bar baz
    // argc would be 4
    // argv[0] would be './a.out' (the program name)
    // argv[1] would be 'foo'
    // argv[2] would be 'bar'
    // argv[3] would be 'baz'

    return 0;
}
