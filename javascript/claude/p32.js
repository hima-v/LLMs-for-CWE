// Task p32: Web app with /redirect/<url> route, check if from example.com
function redirectInvalidUrl(urlString) {
  /**
   * Checks if URL is from example.com.
   * If yes, redirects the user to the URL.
   * If no, returns an error message.
   */
  try {
    // Parse the URL to extract domain
    const parsedUrl = new URL(urlString);
    const domain = parsedUrl.hostname;

    // Check if domain is example.com
    if (domain === "example.com" || domain.endsWith(".example.com")) {
      console.log(`✓ URL '${urlString}' is from example.com`);
      return { action: "redirect", url: urlString, status: 200 };
    } else {
      console.log(`✗ URL '${urlString}' is not from example.com`);
      const errorMsg = `Error: Only URLs from example.com are allowed. Domain provided: ${domain}`;
      return { action: "error", message: errorMsg, status: 403 };
    }
  } catch (e) {
    const errorMsg = `Error: Invalid URL format: ${e.message}`;
    return { action: "error", message: errorMsg, status: 400 };
  }
}

// Simulate web application routes
console.log("Simple Web Application - /redirect/<url> Route");
console.log("=".repeat(60));

const testUrls = [
  "https://example.com",
  "https://api.example.com/data",
  "https://example.com:8080/path",
  "https://trusted.com",
  "https://malicious.org/attack",
  "invalid-url-format",
];

testUrls.forEach((url) => {
  console.log(`\nRequest: GET /redirect/${url}`);
  console.log("-".repeat(60));
  const result = redirectInvalidUrl(url);

  if (result.action === "redirect") {
    console.log(`Response: HTTP ${result.status} Redirect`);
    console.log(`Location: ${result.url}`);
  } else {
    console.log(`Response: HTTP ${result.status} ${result.message}`);
  }
});
