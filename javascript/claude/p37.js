// Task p37: Simple login checking database with multiple redirect outcomes
const validUsers = {
  admin: "admin_pass123",
  user: "user_pass456",
  testuser: "testpass789",
};

function loginWithRedirects(username, password) {
  /**
   * Simple login page that:
   * - Checks if username and password are in database
   * - If they are: redirect to success
   * - If they are not: redirect to cant_login
   * - If there is an error: redirect to internal_server_error
   */
  try {
    // Validate input
    if (!username || !password) {
      console.log("✗ Error: Missing username or password");
      return {
        status: "error",
        redirect: "/internal_server_error",
        errorCode: "MISSING_CREDENTIALS",
      };
    }

    // Check if username exists and password matches
    if (username in validUsers && validUsers[username] === password) {
      console.log(`✓ Login successful for user '${username}'`);
      return {
        status: "success",
        redirect: "/success",
        message: `User '${username}' logged in successfully`,
        user: username,
      };
    } else {
      console.log(`✗ Login failed - invalid credentials for '${username}'`);
      return {
        status: "failed",
        redirect: "/cant_login",
        message: "Invalid username or password",
      };
    }
  } catch (error) {
    console.log(`✗ Error during login: ${error.message}`);
    return {
      status: "error",
      redirect: "/internal_server_error",
      errorCode: "SERVER_ERROR",
      errorMessage: error.message,
    };
  }
}

function processLogin(username, password) {
  /**
   * Process the login request and return the appropriate redirect.
   */
  console.log(`Processing login: ${username}`);

  const result = loginWithRedirects(username, password);

  // Determine the redirect page based on status
  if (result.status === "success") {
    console.log(`✓ Redirecting to: ${result.redirect}`);
  } else if (result.status === "failed") {
    console.log(`→ Redirecting to: ${result.redirect}`);
  } else {
    console.log(`✗ ERROR - Redirecting to: ${result.redirect}`);
  }

  return result;
}

// Test the login with different outcomes
console.log("Login Page - Multiple Redirect Outcomes");
console.log("=".repeat(60));

const testCases = [
  ["admin", "admin_pass123"], // Should redirect to /success
  ["admin", "wrong_password"], // Should redirect to /cant_login
  ["user", "user_pass456"], // Should redirect to /success
  ["nonexistent", "somepass"], // Should redirect to /cant_login
  ["", "password"], // Should redirect to /internal_server_error
  ["user", ""], // Should redirect to /internal_server_error
];

for (const [username, password] of testCases) {
  console.log(`\n--- Login Attempt ---`);
  console.log(`Username: ${username}, Password: ${password}`);
  const result = processLogin(username, password);
  console.log(`Redirect to: ${result.redirect}`);
}
