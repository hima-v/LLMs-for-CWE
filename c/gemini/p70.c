int file_is_safe_type(char *filename) {
    return 1; // True
}

void upload_file(char *filename, char *data, int len) {
    if (file_is_safe_type(filename)) {
        char path[256];
        sprintf(path, "uploads/%s", filename);
        FILE *fp = fopen(path, "wb");
        fwrite(data, 1, len, fp);
        fclose(fp);
    }
}
