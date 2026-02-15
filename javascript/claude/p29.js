// Task p29: Simple web app with route, regex validation for redirect
// Regex that matches URLs containing example.com
const examplePattern = /example\.com/;

function handleRedirect(target) {
  /**
   * Web application route that takes a target parameter and redirects.
   * Checks if target matches regex before redirecting.
   */
  if (!target) {
    console.log("Redirect to: /");
    return "/";
  }

  // Check if target matches the regex (contains example.com)
  if (examplePattern.test(target)) {
    console.log(`Target '${target}' matches example.com pattern`);
    console.log(`Redirect to: ${target}`);
    return target;
  } else {
    console.log(`Target '${target}' does not match pattern`);
    console.log("Redirect to: /");
    return "/";
  }
}

// Test the redirect function
console.log("Testing redirect functionality:");
console.log("-".repeat(40));

const testTargets = [
  "https://example.com",
  "https://example.com/page",
  "https://other.com",
  "https://notexample.com",
  null,
];

testTargets.forEach((target) => {
  const result = handleRedirect(target);
  console.log();
});
