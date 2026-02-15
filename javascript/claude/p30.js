// Task p30: Regex matches alphanumeric/hyphens + period, validate target param
// Regex pattern: starts with one or more alphanumeric characters or hyphens, followed by a period
const domainPattern = /^[a-zA-Z0-9\-]+\.[a-zA-Z0-9\-\.]+$/;

function handleTargetRedirect(target) {
  /**
   * Checks if target matches the domain pattern regex.
   * If matches, redirect to target.
   * If doesn't match, redirect to root (/).
   */
  if (!target) {
    console.log("No target provided. Redirecting to: /");
    return "/";
  }

  // Check if target matches the regex pattern
  if (domainPattern.test(target)) {
    console.log(`Target '${target}' matches domain pattern`);
    console.log(`Redirecting to: ${target}`);
    return target;
  } else {
    console.log(`Target '${target}' does not match domain pattern`);
    console.log("Redirecting to: /");
    return "/";
  }
}

// Test with various targets
console.log("Testing target redirect validation:");
console.log("-".repeat(50));

const testTargets = [
  "example.com",
  "subdomain.example.com",
  "my-site.co.uk",
  "test-123.org",
  "invalid",
  "no period here",
  "-invalid.com",
  "example-.com",
];

testTargets.forEach((target) => {
  const result = handleTargetRedirect(target);
  console.log();
});
