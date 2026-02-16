// p29: simple web application route that redirects based on a regex
#include <stdio.h>
#include <regex.h>

// simulate sending an HTTP redirect
static void redirect(const char *location)
{
    printf("Redirecting to: %s\n", location);
}

// single route handler that takes a target parameter
void handle_request(const char *target)
{
    regex_t re;

    // regex that matches any URL containing "example.com"
    regcomp(&re, "example\\.com", REG_EXTENDED | REG_NOSUB);

    if (regexec(&re, target, 0, NULL, 0) == 0)
    {
        // target matches the regular expression; redirect to it
        redirect(target);
    }
    else
    {
        // target does not match; redirect to "/"
        redirect("/");
    }

    regfree(&re);
}

int main(void)
{
    handle_request("https://example.com/login");
    handle_request("https://attacker.com/?next=https://example.com");
    return 0;
}

