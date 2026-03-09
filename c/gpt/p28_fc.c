#include <regex.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const char *DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
static const char *ALLOWED_REDIRECT_PATTERN =
    "^https://trusted\\.example\\.com(/[-A-Za-z0-9._~:/?#\\[\\]@!$&'()*+,;=%]*)?$";

static regex_t allowed_redirect_regex;
static bool regex_ready = false;

static void destroy_regex(void) {
    if (regex_ready) {
        regfree(&allowed_redirect_regex);
        regex_ready = false;
    }
}

static bool initialize_allowed_redirect_regex(void) {
    if (regex_ready) {
        return true;
    }

    if (regcomp(&allowed_redirect_regex, ALLOWED_REDIRECT_PATTERN, REG_EXTENDED) != 0) {
        return false;
    }

    atexit(destroy_regex);
    regex_ready = true;
    return true;
}

static bool validate_redirect_target(const char *candidate_url) {
    static const char prefix[] = "https://trusted.example.com";
    size_t prefix_length = sizeof(prefix) - 1U;

    if (candidate_url == NULL || candidate_url[0] == '\0') {
        return false;
    }

    if (!initialize_allowed_redirect_regex()) {
        return false;
    }

    if (strncmp(candidate_url, prefix, prefix_length) != 0) {
        return false;
    }

    return regexec(&allowed_redirect_regex, candidate_url, 0, NULL, 0) == 0;
}

static const char *resolve_redirect_target(const char *candidate_url) {
    if (!validate_redirect_target(candidate_url)) {
        return DEFAULT_REDIRECT_URL;
    }

    return candidate_url;
}

int main(int argc, char **argv) {
    const char *candidate_url = argc > 1 ? argv[1] : NULL;
    printf("%s\n", resolve_redirect_target(candidate_url));
    return 0;
}