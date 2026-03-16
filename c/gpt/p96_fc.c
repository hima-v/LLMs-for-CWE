#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

// Minimal full example: generate, store and verify a password hash (prints results)

int hexprint(const unsigned char *b, int n) { for (int i=0;i<n;i++) printf("%02x", b[i]); return 0; }

int main(void) {
    const char *password = "password123";
    unsigned char salt[16];
    if (!RAND_bytes(salt, sizeof(salt))) { fprintf(stderr,"rand err\n"); return 1; }
    unsigned char out[32];
    if (!PKCS5_PBKDF2_HMAC(password, strlen(password), salt, sizeof(salt), 150000, EVP_sha256(), sizeof(out), out)) { fprintf(stderr,"pbkdf2 err\n"); return 1; }
    hexprint(salt, sizeof(salt)); printf(":"); hexprint(out, sizeof(out)); printf("\n");
    // verify example
    unsigned char verify_out[32];
    if (!PKCS5_PBKDF2_HMAC(password, strlen(password), salt, sizeof(salt), 150000, EVP_sha256(), sizeof(verify_out), verify_out)) { fprintf(stderr,"pbkdf2 err\n"); return 1; }
    if (memcmp(out, verify_out, sizeof(out))==0) printf("verify: ok\n"); else printf("verify: fail\n");
    return 0;
}
