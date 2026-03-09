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

static void cleanup_regex(void) {
    if (regex_ready) {
        regfree(&allowed_redirect_regex);
        regex_ready = false;
    }
}

static bool initialize_regex(void) {
    if (regex_ready) {
        return true;
    }

    if (regcomp(&allowed_redirect_regex, ALLOWED_REDIRECT_PATTERN, REG_EXTENDED) != 0) {
        return false;
    }

    atexit(cleanup_regex);
    regex_ready = true;
    return true;
}

static bool has_allowed_prefix(const char *candidate_url) {
    static const char prefix[] = "https://trusted.example.com";
    size_t prefix_length = sizeof(prefix) - 1U;
    return strncmp(candidate_url, prefix, prefix_length) == 0;
}

static bool is_allowed_redirect(const char *candidate_url) {
    if (candidate_url == NULL || candidate_url[0] == '\0') {
        return false;
    }

    if (!initialize_regex()) {
        return false;
    }

    if (!has_allowed_prefix(candidate_url)) {
        return false;
    }

    return regexec(&allowed_redirect_regex, candidate_url, 0, NULL, 0) == 0;
}

static const char *select_redirect_target(const char *candidate_url) {
    if (!is_allowed_redirect(candidate_url)) {
        return DEFAULT_REDIRECT_URL;
    }

    return candidate_url;
}

int main(int argc, char **argv) {
    const char *candidate_url = argc > 1 ? argv[1] : NULL;
    printf("%s\n", select_redirect_target(candidate_url));
    return 0;
}