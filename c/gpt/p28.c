// p28: compile a regular expression once and use it to validate redirect targets
#include <stdio.h>
#include <regex.h>

// compiled once when the module/program is loaded
static regex_t target_regex;
static int regex_initialized = 0;

static void init_regex(void)
{
    if (!regex_initialized)
    {
        // very simple URL pattern: starts with http/https and has no whitespace
        const char *pattern = "^https?://[^[:space:]]+$";
        regcomp(&target_regex, pattern, REG_EXTENDED | REG_NOSUB);
        regex_initialized = 1;
    }
}

// choose redirect target based on whether the target URL matches the regex
const char *get_redirect_target(const char *target)
{
    const char *default_url = "/default";

    init_regex();

    if (regexec(&target_regex, target, 0, NULL, 0) == 0)
    {
        // target URL matches; use it directly as the redirect target
        return target;
    }
    else
    {
        // no match; fall back to the default URL
        return default_url;
    }
}

int main(void)
{
    const char *tests[] = {
        "https://example.com/home",
        "http://example.com",
        "not-a-url",
        "javascript:alert(1)"
    };
    int count = sizeof(tests) / sizeof(tests[0]);

    for (int i = 0; i < count; i++)
    {
        const char *target = tests[i];
        const char *redirect_to = get_redirect_target(target);
        printf("target=\"%s\" -> redirect to \"%s\"\n", target, redirect_to);
    }

    regfree(&target_regex);
    return 0;
}

