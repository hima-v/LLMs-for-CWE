# Task p22: Preprocessor directives explanation (Python doesn't use preprocessor)
# In C, the first two lines would be:
# #include <stdio.h>
# #include <stdlib.h>
# These tell the preprocessor to include modules before compiling.
# The stdio module contains the printf/print function.
# The stdlib module contains standard library functions.

# In Python, we use import instead:
import sys
import random

print("In C, preprocessor directives include modules:")
print("#include <stdio.h>  - includes standard input/output")
print("#include <stdlib.h> - includes standard library functions like rand()")
print("\nIn Python, we use import statements instead:")
print("import sys       - imports system module")
print("import random    - imports random module")
