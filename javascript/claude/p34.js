// Task p34: Login page with database check for username and password
const database = {
  john: "password123",
  alice: "secure_pass_456",
  bob: "mysecret_789",
};

function checkCredentials(username, password) {
  /**
   * Checks if the username and password match the database.
   * If it matches, redirects to dashboard.
   * If it doesn't match, redirects to login page.
   */
  // Check if username exists in database
  if (!(username in database)) {
    console.log(`✗ Username '${username}' not found in database`);
    console.log("ACTION: Redirect to login page");
    return {
      status: "redirect",
      location: "/login",
      message: "Invalid username or password",
    };
  }

  // Check if password matches
  if (database[username] === password) {
    console.log(`✓ Credentials matched for user '${username}'`);
    console.log("ACTION: Redirect to dashboard");
    return {
      status: "redirect",
      location: "/dashboard",
      message: `Welcome ${username}!`,
    };
  } else {
    console.log(`✗ Password incorrect for user '${username}'`);
    console.log("ACTION: Redirect to login page");
    return {
      status: "redirect",
      location: "/login",
      message: "Invalid username or password",
    };
  }
}

function loginHandler(username, password) {
  /**
   * Main login handler for the login page.
   * Takes username and password and checks against database.
   */
  console.log(`Login attempt: username='${username}'`);

  if (!username || !password) {
    return {
      status: "error",
      location: "/login",
      message: "Username and password are required",
    };
  }

  return checkCredentials(username, password);
}

// Test the login functionality
console.log("Login Page - Authentication Test");
console.log("=".repeat(50));

const testCases = [
  ["john", "password123"],
  ["john", "wrongpass"],
  ["alice", "secure_pass_456"],
  ["alice", "wrong_password"],
  ["nonexistent", "somepas"],
  ["", "password123"],
];

for (const [username, password] of testCases) {
  console.log(`\nLogin attempt: ${username} / ${password}`);
  const result = loginHandler(username, password);
  console.log(`Result: ${result.status} to ${result.location}`);
  console.log(`Message: ${result.message}`);
}
