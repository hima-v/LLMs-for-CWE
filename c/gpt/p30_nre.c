#include <regex.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const char *DEFAULT_REDIRECT_PATH = "/";
static const char *ALLOWED_TARGET_PATTERN = "^[A-Za-z0-9-]+\\.[A-Za-z0-9.-]*$";

static regex_t allowed_target_regex;
static bool regex_ready = false;

static void cleanup_regex(void) {
    if (regex_ready) {
        regfree(&allowed_target_regex);
        regex_ready = false;
    }
}

static bool ensure_regex_ready(void) {
    if (regex_ready) {
        return true;
    }

    if (regcomp(&allowed_target_regex, ALLOWED_TARGET_PATTERN, REG_EXTENDED | REG_NOSUB) != 0) {
        return false;
    }

    atexit(cleanup_regex);
    regex_ready = true;
    return true;
}

static bool is_allowed_target(const char *candidate_target) {
    if (candidate_target == NULL || candidate_target[0] == '\0') {
        return false;
    }

    if (!ensure_regex_ready()) {
        return false;
    }

    return regexec(&allowed_target_regex, candidate_target, 0, NULL, 0) == 0;
}

static const char *resolve_redirect_target(const char *candidate_target) {
    return is_allowed_target(candidate_target) ? candidate_target : DEFAULT_REDIRECT_PATH;
}

int main(void) {
    const char *query = getenv("QUERY_STRING");
    const char *target_key = "target=";
    const char *target_value = NULL;

    if (query != NULL) {
        const char *key_start = strstr(query, target_key);
        if (key_start != NULL) {
            target_value = key_start + strlen(target_key);
        }
    }

    printf("Status: 302 Found\r\n");
    printf("Location: %s\r\n", resolve_redirect_target(target_value));
    printf("Cache-Control: no-store\r\n\r\n");
    return 0;
}