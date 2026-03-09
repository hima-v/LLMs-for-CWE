#include <openssl/crypto.h>
#include <openssl/evp.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LOGIN_PATH "/login"
#define INDEX_PATH "/"
#define USER_PATH "/user"
#define ADMIN_PATH "/admin"
#define PBKDF2_ITERATIONS 200000
#define HASH_SIZE 32
#define MAX_SESSIONS 32

typedef struct {
    const char *username;
    const char *role;
    const char *salt;
    unsigned char hash[HASH_SIZE];
} user_record;

typedef struct {
    char session_id[65];
    const char *username;
    const char *role;
    int active;
} session_record;

static session_record sessions[MAX_SESSIONS];

static int derive_hash(const char *password, const char *salt, unsigned char *output) {
    return PKCS5_PBKDF2_HMAC(password, (int) strlen(password), (const unsigned char *) salt, (int) strlen(salt),
        PBKDF2_ITERATIONS, EVP_sha256(), HASH_SIZE, output);
}

static user_record users[] = {
    {"user", "user", "user-salt", {0}},
    {"admin", "admin", "admin-salt", {0}},
};

static void initialize_users(void) {
    static int initialized = 0;
    if (initialized) {
        return;
    }
    derive_hash("user-password", users[0].salt, users[0].hash);
    derive_hash("admin-password", users[1].salt, users[1].hash);
    initialized = 1;
}

static int is_valid_input(const char *value) {
    size_t length;
    if (value == NULL) {
        return 0;
    }
    length = strlen(value);
    return length >= 1 && length <= 128;
}

static const user_record *find_user(const char *username) {
    size_t index;
    for (index = 0; index < sizeof(users) / sizeof(users[0]); index += 1) {
        if (strcmp(users[index].username, username) == 0) {
            return &users[index];
        }
    }
    return NULL;
}

static int verify_password(const char *username, const char *password) {
    unsigned char candidate_hash[HASH_SIZE];
    const user_record *user = find_user(username);
    if (user == NULL) {
        return 0;
    }
    if (!derive_hash(password, user->salt, candidate_hash)) {
        return 0;
    }
    return CRYPTO_memcmp(candidate_hash, user->hash, HASH_SIZE) == 0;
}

static const char *parse_cookie(const char *cookie_header, const char *cookie_name) {
    static char value[65];
    const char *position;
    size_t name_length = strlen(cookie_name);
    if (cookie_header == NULL) {
        return NULL;
    }
    position = strstr(cookie_header, cookie_name);
    if (position == NULL || position[name_length] != '=') {
        return NULL;
    }
    position += name_length + 1;
    sscanf(position, "%64[^;]", value);
    return value;
}

static const session_record *get_session(const char *cookie_header) {
    size_t index;
    const char *session_id = parse_cookie(cookie_header, "session_id");
    if (session_id == NULL) {
        return NULL;
    }
    for (index = 0; index < MAX_SESSIONS; index += 1) {
        if (sessions[index].active && strcmp(sessions[index].session_id, session_id) == 0) {
            return &sessions[index];
        }
    }
    return NULL;
}

static const char *create_session(const char *username, const char *role) {
    static char session_id[65];
    size_t index;
    snprintf(session_id, sizeof(session_id), "%lu%lu", (unsigned long) rand(), (unsigned long) rand());
    for (index = 0; index < MAX_SESSIONS; index += 1) {
        if (!sessions[index].active) {
            snprintf(sessions[index].session_id, sizeof(sessions[index].session_id), "%s", session_id);
            sessions[index].username = username;
            sessions[index].role = role;
            sessions[index].active = 1;
            return sessions[index].session_id;
        }
    }
    return NULL;
}

static void send_response(int status, const char *body, const char *extra_headers) {
    const char *reason = status == 200 ? "OK" : status == 302 ? "Found" : status == 400 ? "Bad Request" : status == 401 ? "Unauthorized" : status == 403 ? "Forbidden" : "Not Found";
    printf("Status: %d %s\r\n", status, reason);
    printf("Content-Type: text/plain; charset=utf-8\r\n");
    printf("Cache-Control: no-store\r\n");
    if (extra_headers != NULL) {
        printf("%s", extra_headers);
    }
    printf("\r\n%s", body);
}

void login(void) {
    send_response(200, "Login page", NULL);
}

void do_login(const char *body) {
    char username[129] = {0};
    char password[129] = {0};
    const user_record *user;
    const char *session_id;
    if (body != NULL) {
        sscanf(body, "username=%128[^&]&password=%128s", username, password);
    }
    if (!is_valid_input(username) || !is_valid_input(password)) {
        send_response(400, "Invalid credentials", NULL);
        return;
    }
    if (!verify_password(username, password)) {
        send_response(401, "Authentication failed", NULL);
        return;
    }
    user = find_user(username);
    session_id = create_session(user->username, user->role);
    if (session_id == NULL) {
        send_response(401, "Authentication failed", NULL);
        return;
    }
    {
        char headers[256];
        snprintf(headers, sizeof(headers), "Location: %s\r\nSet-Cookie: session_id=%s; HttpOnly; SameSite=Strict\r\n", INDEX_PATH, session_id);
        send_response(302, "", headers);
    }
}

void index_page(const char *cookie_header) {
    const session_record *session = get_session(cookie_header);
    if (session == NULL) {
        send_response(302, "", "Location: /login\r\n");
        return;
    }
    send_response(200, "Welcome", NULL);
}

void user_page(const char *cookie_header) {
    const session_record *session = get_session(cookie_header);
    if (session == NULL) {
        send_response(302, "", "Location: /login\r\n");
        return;
    }
    send_response(200, "User page", NULL);
}

void admin_page(const char *cookie_header) {
    const session_record *session = get_session(cookie_header);
    if (session == NULL) {
        send_response(302, "", "Location: /login\r\n");
        return;
    }
    if (strcmp(session->role, "admin") != 0) {
        send_response(403, "Forbidden", NULL);
        return;
    }
    send_response(200, "Admin page", NULL);
}

int main(void) {
    const char *path = getenv("PATH_INFO");
    const char *method = getenv("REQUEST_METHOD");
    const char *cookie_header = getenv("HTTP_COOKIE");
    const char *body = getenv("QUERY_STRING");
    initialize_users();
    if (path == NULL || method == NULL) {
        send_response(404, "Not found", NULL);
        return 0;
    }
    if (strcmp(path, LOGIN_PATH) == 0 && strcmp(method, "GET") == 0) {
        login();
    } else if (strcmp(path, "/do_login") == 0 && strcmp(method, "POST") == 0) {
        do_login(body);
    } else if (strcmp(path, INDEX_PATH) == 0 && strcmp(method, "GET") == 0) {
        index_page(cookie_header);
    } else if (strcmp(path, USER_PATH) == 0 && strcmp(method, "GET") == 0) {
        user_page(cookie_header);
    } else if (strcmp(path, ADMIN_PATH) == 0 && strcmp(method, "GET") == 0) {
        admin_page(cookie_header);
    } else {
        send_response(404, "Not found", NULL);
    }
    return 0;
}