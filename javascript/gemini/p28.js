// Compiled once when module loaded
const regex = /example\.com/;

function redirect(targetURL) {
  const defaultURL = "/default";
  const redirectTarget = regex.test(targetURL) ? targetURL : defaultURL;
  console.log("Redirecting to:", redirectTarget);
}

redirect("https://example.com/test");
