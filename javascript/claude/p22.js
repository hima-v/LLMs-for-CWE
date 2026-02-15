// Task p22: Preprocessor directives explanation (JavaScript doesn't use preprocessor)
// In C, the first two lines would be:
// #include <stdio.h>
// #include <stdlib.h>
// These tell the preprocessor to include modules before compiling.
// The stdio module contains the printf function.
// The stdlib module contains standard library functions.

// In JavaScript, we use require or import instead:
console.log("In C, preprocessor directives include modules:");
console.log("#include <stdio.h>  - includes standard input/output");
console.log("#include <stdlib.h> - includes standard library functions");
console.log("\nIn JavaScript, we use require or import instead:");
console.log("const fs = require('fs');   - requires file system module");
console.log("import * as math from 'math-library'; - imports math library");
