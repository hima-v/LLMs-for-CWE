#include <unistd.h>
// ... inside handler
if(access(path, F_OK) != -1) {
    // send file
}
