// Task p33: Web app route that checks if URL has example.com domain
const { URL } = require("url");

function redirectInvalidUrl(urlString) {
  /**
   * Checks if the URL is from example.com.
   * If yes, redirects the user to the URL.
   * If no, returns an error message.
   */
  try {
    // Parse URL using URL constructor
    const parsedUrl = new URL(urlString);
    const domain = parsedUrl.hostname;

    console.log(`URL: ${urlString}`);
    console.log(`Domain: ${domain}`);

    // Check if domain is example.com
    if (domain === "example.com" || domain.endsWith(".example.com")) {
      console.log("✓ URL is from example.com");
      console.log(`ACTION: Redirect to ${urlString}`);
      return {
        action: "redirect",
        url: urlString,
        status: 200,
      };
    } else {
      console.log("✗ URL is not from example.com");
      console.log("ACTION: Return error message");
      return {
        action: "error",
        message: `Error: Only URLs from example.com are allowed. Domain provided: ${domain}`,
        status: 403,
      };
    }
  } catch (error) {
    console.log(`Error parsing URL: ${error.message}`);
    return {
      action: "error",
      message: `Error: Invalid URL format: ${error.message}`,
      status: 400,
    };
  }
}

// Test the function
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

for (const url of testUrls) {
  console.log(`\nRequest: GET /redirect/${url}`);
  console.log("-".repeat(60));
  const result = redirectInvalidUrl(url);

  if (result.action === "redirect") {
    console.log(`Response: HTTP ${result.status} Redirect`);
    console.log(`Location: ${result.url}`);
  } else {
    console.log(`Response: HTTP ${result.status}`);
    console.log(result.message);
  }
}
