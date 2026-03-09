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

static void release_regex(void) {
    if (regex_ready) {
        regfree(&allowed_redirect_regex);
        regex_ready = false;
    }
}

static bool ensure_regex_ready(void) {
    if (regex_ready) {
        return true;
    }

    if (regcomp(&allowed_redirect_regex, ALLOWED_REDIRECT_PATTERN, REG_EXTENDED) != 0) {
        return false;
    }

    atexit(release_regex);
    regex_ready = true;
    return true;
}

static bool has_required_shape(const char *candidate_url) {
    static const char prefix[] = "https://trusted.example.com";
    size_t prefix_length = sizeof(prefix) - 1U;
    return candidate_url != NULL && strncmp(candidate_url, prefix, prefix_length) == 0;
}

static bool matches_allowlist(const char *candidate_url) {
    return regexec(&allowed_redirect_regex, candidate_url, 0, NULL, 0) == 0;
}

static const char *get_safe_redirect_target(const char *candidate_url) {
    if (candidate_url == NULL || candidate_url[0] == '\0') {
        return DEFAULT_REDIRECT_URL;
    }

    if (!ensure_regex_ready()) {
        return DEFAULT_REDIRECT_URL;
    }

    if (!has_required_shape(candidate_url)) {
        return DEFAULT_REDIRECT_URL;
    }

    if (!matches_allowlist(candidate_url)) {
        return DEFAULT_REDIRECT_URL;
    }

    return candidate_url;
}

int main(int argc, char **argv) {
    const char *candidate_url = argc > 1 ? argv[1] : NULL;
    printf("%s\n", get_safe_redirect_target(candidate_url));
    return 0;
}