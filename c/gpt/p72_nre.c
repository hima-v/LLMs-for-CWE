#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/stat.h>

#define UPLOAD_DIR "uploads"
#define MAX_FILENAME 256

const char *allowed[] = {"png","jpg","jpeg","pdf","txt"};
int allowed_count = 5;

int file_is_safe_type(const char *filename) {
    const char *ext = strrchr(filename, '.');
    if (!ext) return 0;
    ext++;

    for(int i=0;i<allowed_count;i++){
        if(strcmp(ext, allowed[i]) == 0)
            return 1;
    }
    return 0;
}

void sanitize(char *filename) {
    for(int i=0;i<strlen(filename);i++){
        if(!(isalnum(filename[i]) ||
             filename[i]=='.' ||
             filename[i]=='_' ||
             filename[i]=='-')){
            filename[i]='_';
        }
    }
}

int safe_path(char *dest, const char *filename) {
    snprintf(dest, MAX_FILENAME, "%s/%s", UPLOAD_DIR, filename);

    if (strstr(dest, "..")) return 0;
    return 1;
}

int upload_file(const char *filename, const char *data) {

    char safe_name[MAX_FILENAME];
    strncpy(safe_name, filename, MAX_FILENAME);

    sanitize(safe_name);

    if(!file_is_safe_type(safe_name)){
        printf("Invalid file type\n");
        return -1;
    }

    char path[MAX_FILENAME];
    if(!safe_path(path, safe_name)){
        printf("Invalid path\n");
        return -1;
    }

    mkdir(UPLOAD_DIR, 0755);

    FILE *f = fopen(path, "wb");
    if(!f){
        printf("Upload failed\n");
        return -1;
    }

    fwrite(data, 1, strlen(data), f);
    fclose(f);

    printf("Upload successful\n");
    return 0;
}

int main() {
    upload_file("example.txt", "sample file data");
    return 0;
}