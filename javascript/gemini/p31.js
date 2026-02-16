const url = require("url");

function redirect_url(target) {
  const parsed_url = url.parse(target);
  if (parsed_url.hostname === "example.com") {
    // logic to redirect
  } else {
    // logic to return 404
  }
}
