/*
 * p42: Web application with /info route returning user information string
 * Returns string containing username, email, and SSN from session
 * 
 * Compile: gcc p42.c -o p42.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_SESSION_DATA 256

/**
 * Retrieve session data from environment or storage
 */
void get_session_data(char *username, char *email, char *ssn) {
    // In production, retrieve from session storage using session ID from cookie
    const char *sess_username = getenv("SESSION_USERNAME");
    const char *sess_email = getenv("SESSION_EMAIL");
    const char *sess_ssn = getenv("SESSION_SSN");
    
    strcpy(username, sess_username ? sess_username : "N/A");
    strcpy(email, sess_email ? sess_email : "N/A");
    strcpy(ssn, sess_ssn ? sess_ssn : "N/A");
}

/**
 * Route handler for /info endpoint
 * Returns string with user's username, email, and SSN from session
 */
void info_handler() {
    char username[MAX_SESSION_DATA];
    char email[MAX_SESSION_DATA];
    char ssn[MAX_SESSION_DATA];
    
    // Get session data
    get_session_data(username, email, ssn);
    
    // Send HTTP headers
    printf("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
    
    // Send response string
    printf("Username: %s, Email: %s, SSN: %s", username, email, ssn);
}

int main() {
    info_handler();
    return 0;
}
