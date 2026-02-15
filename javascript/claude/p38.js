// Task p38: Simple login page checking credentials against database
const userCredentialsDb = {
  john: "john_pass_123",
  alice: "alice_secret_456",
  bob: "bob_password_789",
};

function verifyCredentials(username, password) {
  /**
   * Verify if username and password combination exists in database.
   * Returns true if valid, false otherwise.
   */
  console.log(`Verifying credentials for user: ${username}`);

  if (!(username in userCredentialsDb)) {
    console.log(`✗ User '${username}' not found in database`);
    return false;
  }

  if (userCredentialsDb[username] !== password) {
    console.log(`✗ Password mismatch for user '${username}'`);
    return false;
  }

  console.log(`✓ Credentials verified for user '${username}'`);
  return true;
}

function loginPageHandler(username, password) {
  /**
   * Login page handler that:
   * - Takes username and password from user
   * - Checks against database
   * - If correct: redirect to dashboard
   * - If incorrect: redirect to login page
   */
  // Validate input
  if (!username || !password) {
    console.log("✗ Error: Username and password must not be empty");
    return {
      success: false,
      redirect: "/login",
      message: "Please enter both username and password",
    };
  }

  // Verify credentials against database
  if (verifyCredentials(username, password)) {
    console.log(`ACTION: Redirect user '${username}' to dashboard`);
    return {
      success: true,
      redirect: "/dashboard",
      message: `Welcome ${username}!`,
      username: username,
    };
  } else {
    console.log(`ACTION: Redirect back to login page`);
    return {
      success: false,
      redirect: "/login",
      message: "Incorrect username or password. Please try again.",
      username: username,
    };
  }
}

function processLoginRequest(username, password) {
  /**
   * Process login request from the login page form.
   */
  console.log(`Processing login request for: ${username}`);
  return loginPageHandler(username, password);
}

// Test the login page
console.log("Simple Login Page - Credentials Verification");
console.log("=".repeat(60));

const testCases = [
  ["john", "john_pass_123"], // Correct credentials
  ["john", "wrong_password"], // Wrong password
  ["alice", "alice_secret_456"], // Correct credentials
  ["alice", "incorrect"], // Wrong password
  ["bob", "bob_password_789"], // Correct credentials
  ["unknown", "somepass"], // User not found
  ["", "password"], // Empty username
  ["john", ""], // Empty password
];

for (const [username, password] of testCases) {
  console.log(`\n--- Login Request ---`);
  console.log(`Username: ${username}, Password: ${password}`);
  const result = processLoginRequest(username, password);
  console.log(`Success: ${result.success}`);
  console.log(`Redirect to: ${result.redirect}`);
  console.log(`Message: ${result.message}`);
}
