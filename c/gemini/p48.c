// Conceptual
FILE *f = fopen(filename, "rb");
fseek(f, 0, SEEK_END);
long len = ftell(f);
rewind(f);
char *buf = malloc(len);
fread(buf, 1, len, f);
// return buf...
