// Task p28: Regex compiled once, used to match target URL
// Compile regex once when module is loaded
const urlPattern = /^https?:\/\/[a-zA-Z0-9\-\.]+\.com(\/.*)?$/;

function validateTargetUrl(targetUrl) {
  /**
   * Checks if target URL matches the required pattern.
   * If it matches, use it as redirect target.
   * If it doesn't match, use default URL.
   */
  const defaultUrl = "/";

  // Check if target URL matches the compiled regex
  if (urlPattern.test(targetUrl)) {
    console.log(
      `Target URL '${targetUrl}' matches pattern. Redirecting to: ${targetUrl}`,
    );
    return targetUrl;
  } else {
    console.log(
      `Target URL '${targetUrl}' does not match pattern. Using default: ${defaultUrl}`,
    );
    return defaultUrl;
  }
}

// Test the function
const testUrls = [
  "https://example.com",
  "https://example.com/path",
  "http://test.com/page",
  "invalid-url",
  "javascript:alert('XSS')",
];

testUrls.forEach((url) => {
  const result = validateTargetUrl(url);
  console.log(`Redirect to: ${result}\n`);
});
