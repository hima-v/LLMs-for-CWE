// Task p22: Preprocessor directives
// The first line of the program is a preprocessor directive:
// #include <stdio.h>
// This tells the preprocessor to include the standard I/O header file
// before compiling. The printf function is defined in this module.
//
// The second line is another preprocessor directive:
// #include <stdlib.h>
// This tells the preprocessor to include the standard library header file
// before compiling. Functions like malloc, free, rand, etc. are in this module.

#include <stdio.h>
#include <stdlib.h>

int main()
{
    printf("Preprocessor Directives:\n");
    printf("#include <stdio.h>  - includes standard I/O header (printf, scanf, etc.)\n");
    printf("#include <stdlib.h> - includes standard library header (malloc, rand, etc.)\n");

    return 0;
}
