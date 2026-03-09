#include <openssl/crypto.h>
#include <openssl/evp.h>
#include <openssl/sha.h>
#include <sqlite3.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define DATABASE_PATH "users.db"
#define DASHBOARD_PATH "/dashboard"
#define LOGIN_PATH "/login"
#define PBKDF2_ITERATIONS 200000
#define HASH_SIZE 32

static bool validate_input(const char *value) {
    size_t length;

    if (value == NULL) {
        return false;
    }

    length = strlen(value);
    return length >= 1 && length <= 128;
}

static bool hex_to_bytes(const char *hex, unsigned char *output, size_t output_size) {
    size_t index;

    if (strlen(hex) != output_size * 2) {
        return false;
    }

    for (index = 0; index < output_size; index += 1) {
        unsigned int byte_value;
        if (sscanf(hex + (index * 2), "%2x", &byte_value) != 1) {
            return false;
        }
        output[index] = (unsigned char) byte_value;
    }

    return true;
}

static bool verify_password(const char *password, const char *salt, const char *stored_hash_hex) {
    unsigned char candidate_hash[HASH_SIZE];
    unsigned char stored_hash[HASH_SIZE];

    if (!PKCS5_PBKDF2_HMAC(password, (int) strlen(password), (const unsigned char *) salt, (int) strlen(salt),
            PBKDF2_ITERATIONS, EVP_sha256(), HASH_SIZE, candidate_hash)) {
        return false;
    }

    if (!hex_to_bytes(stored_hash_hex, stored_hash, sizeof(stored_hash))) {
        return false;
    }

    return CRYPTO_memcmp(candidate_hash, stored_hash, sizeof(candidate_hash)) == 0;
}

static bool authenticate_user(const char *username, const char *password) {
    sqlite3 *database = NULL;
    sqlite3_stmt *statement = NULL;
    bool authenticated = false;

    if (sqlite3_open_v2(DATABASE_PATH, &database, SQLITE_OPEN_READONLY, NULL) != SQLITE_OK) {
        sqlite3_close(database);
        return false;
    }

    if (sqlite3_prepare_v2(database,
            "SELECT password_salt, password_hash FROM users WHERE username = ?",
            -1,
            &statement,
            NULL) != SQLITE_OK) {
        sqlite3_finalize(statement);
        sqlite3_close(database);
        return false;
    }

    sqlite3_bind_text(statement, 1, username, -1, SQLITE_TRANSIENT);
    if (sqlite3_step(statement) == SQLITE_ROW) {
        const char *salt = (const char *) sqlite3_column_text(statement, 0);
        const char *stored_hash = (const char *) sqlite3_column_text(statement, 1);
        if (salt != NULL && stored_hash != NULL) {
            authenticated = verify_password(password, salt, stored_hash);
        }
    }

    sqlite3_finalize(statement);
    sqlite3_close(database);
    return authenticated;
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    const char *username = NULL;
    const char *password = NULL;
    const char *redirect_target = LOGIN_PATH;

    if (query != NULL) {
        const char *username_key = strstr(query, "username=");
        const char *password_key = strstr(query, "password=");
        if (username_key != NULL) {
            username = username_key + strlen("username=");
        }
        if (password_key != NULL) {
            password = password_key + strlen("password=");
        }
    }

    if (validate_input(username) && validate_input(password) && authenticate_user(username, password)) {
        redirect_target = DASHBOARD_PATH;
    }

    printf("Status: 302 Found\r\n");
    printf("Location: %s\r\n", redirect_target);
    printf("Cache-Control: no-store\r\n\r\n");
    return 0;
}