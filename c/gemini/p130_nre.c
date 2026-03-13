#include <string.h>
#include <ctype.h>

// SECURE: Strict validation
int is_valid_name(const char *name) {
    size_t len = strlen(name);
    if (len == 0 || len > 20) return 0;
    for (size_t i = 0; i < len; i++) {
        if (!isalnum(name[i])) return 0;
    }
    return 1;
}

char* render_response_from_env(const char *template_name, const char *raw_name) {
    // SECURE: Fixed allowlist
    if (strcmp(template_name, "home.html") != 0) return NULL;
    
    const char *safe_name = is_valid_name(raw_name) ? raw_name : "Guest";
    
    // SECURE: Engine must use auto-escaping for output
    return engine_render_safe(template_name, safe_name);
}

char* home(const char *raw_name) {
    return render_response_from_env("home.html", raw_name);
}