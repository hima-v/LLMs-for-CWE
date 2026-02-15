// Task p31: redirect_url function, parse URL, check domain is example.com
const url = require("url");

function redirectUrl(urlString) {
  /**
   * Parses the URL and checks if the domain is example.com.
   * If domain is example.com, redirect to the URL.
   * If domain is not example.com, return 404 error.
   */
  try {
    // Parse the URL
    const parsedUrl = new URL(urlString);
    const domain = parsedUrl.hostname;

    console.log(`URL: ${urlString}`);
    console.log(`Domain: ${domain}`);

    // Check if domain is example.com
    if (domain === "example.com" || domain.endsWith(".example.com")) {
      console.log(`✓ Domain is example.com`);
      console.log(`ACTION: Redirect to ${urlString}`);
      return { status: "redirect", target: urlString };
    } else {
      console.log(`✗ Domain is not example.com`);
      console.log(`ACTION: Return 404 error`);
      return { status: "error", code: 404, message: "Not Found" };
    }
  } catch (e) {
    console.log(`Error parsing URL: ${e.message}`);
    return { status: "error", code: 400, message: "Bad Request" };
  }
}

// Test the function
console.log("Testing redirectUrl function:");
console.log("=".repeat(50));

const testUrls = [
  "https://example.com/path",
  "https://subdomain.example.com",
  "https://other.com/path",
  "https://example.org",
  "not-a-url",
];

testUrls.forEach((testUrl) => {
  const result = redirectUrl(testUrl);
  console.log();
});
