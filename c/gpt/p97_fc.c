#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

int main(void) {
    const char *password = "password123";
    unsigned char salt[16];
    if (!RAND_bytes(salt, sizeof(salt))) { fprintf(stderr,"rand err\n"); return 1; }
    unsigned char out[32];
    if (!PKCS5_PBKDF2_HMAC(password, strlen(password), salt, sizeof(salt), 150000, EVP_sha256(), sizeof(out), out)) { fprintf(stderr,"pbkdf2 err\n"); return 1; }
    for (int i=0;i<sizeof(salt);i++) printf("%02x", salt[i]);
    printf(":");
    for (int i=0;i<sizeof(out);i++) printf("%02x", out[i]);
    printf("\n");
    // verify
    unsigned char verify[32];
    if (!PKCS5_PBKDF2_HMAC(password, strlen(password), salt, sizeof(salt), 150000, EVP_sha256(), sizeof(verify), verify)) { fprintf(stderr,"err\n"); return 1; }
    if (memcmp(out, verify, sizeof(out))==0) printf("verify: ok\n"); else printf("verify: fail\n");
    return 0;
}
