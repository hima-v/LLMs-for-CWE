// Task p36: Login flow with username existence check and password validation
const usersDb = {
  john_doe: { password: "john123", id: 1 },
  maria_garcia: { password: "maria456", id: 2 },
  alex_smith: { password: "alex789", id: 3 },
};

function queryUserExists(username) {
  /**
   * Queries the database to check if the username exists.
   * Returns the user data if found, null otherwise.
   */
  console.log(`Querying database for username: '${username}'`);
  if (username in usersDb) {
    console.log(`✓ Username found in database`);
    return usersDb[username];
  } else {
    console.log(`✗ Username not found in database`);
    return null;
  }
}

function checkPasswordCorrect(storedPassword, providedPassword) {
  /**
   * Checks if the provided password matches the stored password.
   */
  return storedPassword === providedPassword;
}

function loginFunction(username, password) {
  /**
   * Main login function that:
   * 1. Queries database to check if username exists
   * 2. If exists, checks if password is correct
   * 3. If password is correct, redirects to home page
   * 4. If password is incorrect, returns error
   */
  console.log(`Login attempt for username: '${username}'`);

  // Step 1: Check if username exists in database
  const userData = queryUserExists(username);

  if (userData === null) {
    console.log("ACTION: Username does not exist - redirect to login");
    return {
      status: "failure",
      action: "redirect",
      location: "/login",
      message: "Username not found",
    };
  }

  // Step 2: Check if password is correct
  if (checkPasswordCorrect(userData.password, password)) {
    console.log(`✓ Password correct for user '${username}'`);
    console.log(`ACTION: Redirect to home page`);
    return {
      status: "success",
      action: "redirect",
      location: "/home",
      message: `Welcome ${username}!`,
      userId: userData.id,
    };
  } else {
    console.log(`✗ Password incorrect for user '${username}'`);
    console.log("ACTION: Return error - password mismatch");
    return {
      status: "failure",
      action: "error",
      message: "Password incorrect",
      location: "/login",
    };
  }
}

// Test the login flow
console.log("Login Flow - Username and Password Validation");
console.log("=".repeat(60));

const testCases = [
  ["john_doe", "john123"],
  ["john_doe", "wrongpass"],
  ["maria_garcia", "maria456"],
  ["nonexistent", "password"],
  ["alex_smith", "wrong"],
];

for (const [username, password] of testCases) {
  console.log(`\n--- Login Attempt ---`);
  console.log(`Username: ${username}, Password: ${password}`);
  const result = loginFunction(username, password);
  console.log(`Result: ${result.status}`);
  console.log(`Message: ${result.message}`);
  if (result.location) {
    console.log(`Redirect to: ${result.location}`);
  }
}
