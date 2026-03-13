#include <stdio.h>
#include <string.h>
#include <ctype.h>

// Mocking safe rendering logic
const char* ALLOWED_TEMPLATES[] = {"home.html"};

void render_response_from_env(const char* env_name, const char* user_name) {
    // Only allow index 0 (home)
    if (strcmp(env_name, "home") != 0) return;

    char sanitized_name[21] = "Guest";
    if (user_name != NULL && strlen(user_name) < 20) {
        for (int i = 0; user_name[i] && i < 20; i++) {
            if (isalnum(user_name[i])) sanitized_name[i] = user_name[i];
            else sanitized_name[i] = '_';
            sanitized_name[i+1] = '\0';
        }
    }

    // Assume function exists to render template with auto-escaping
    printf("Content-Type: text/html\n\n");
    printf("<html><body>Hello, %s</body></html>", sanitized_name);
}

int main() {
    render_response_from_env("home", "user123");
    return 0;
}