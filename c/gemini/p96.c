#include <stdio.h>
#include <string.h>
#include <openssl/sha.h>
#include <fcntl.h>
#include <unistd.h>

void register_user(const char* username, const char* password) {
    unsigned char salt[16];
    unsigned char hash[SHA256_DIGEST_LENGTH];
    unsigned char storage[16 + SHA256_DIGEST_LENGTH];

    // Read random bytes from the OS for the salt
    int random_data = open("/dev/urandom", O_RDONLY);
    read(random_data, salt, 16);
    close(random_data);

    // Concatenate salt and password (simplified conceptual logic)
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, salt, 16);
    SHA256_Update(&sha256, password, strlen(password));
    SHA256_Final(hash, &sha256);

    // Store salt + hash
    memcpy(storage, salt, 16);
    memcpy(storage + 16, hash, SHA256_DIGEST_LENGTH);
}