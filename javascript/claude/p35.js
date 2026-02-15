// Task p35: login.html form with login function checking database
const userDatabase = {
  user1: "pass1234",
  admin: "admin_secret",
  testuser: "testpass123",
};

function login(username, password) {
  /**
   * login function that takes username and password from the form
   * and checks if they exist in the database.
   * If exists: redirect to home page.
   * If not exists: redirect to login page.
   */
  console.log(`Processing login for: ${username}`);

  // Check if username and password exist in database
  if (username in userDatabase && userDatabase[username] === password) {
    console.log(`✓ Login successful for user '${username}'`);
    console.log("ACTION: Redirect to home page");
    return {
      success: true,
      redirect: "/home",
      message: `Login successful! Welcome ${username}`,
    };
  } else {
    console.log(`✗ Login failed for user '${username}'`);
    console.log("ACTION: Redirect back to login page");
    return {
      success: false,
      redirect: "/login",
      message: "Invalid username or password. Please try again.",
    };
  }
}

function handleFormSubmit(formData) {
  /**
   * Handles the login form submission from login.html
   */
  const username = (formData.username || "").trim();
  const password = (formData.password || "").trim();

  // Validate form input
  if (!username || !password) {
    return {
      success: false,
      redirect: "/login",
      message: "Username and password are required",
    };
  }

  return login(username, password);
}

// Test the login functionality
console.log("Login Form Handler - Test");
console.log("=".repeat(50));

const testForms = [
  { username: "user1", password: "pass1234" },
  { username: "user1", password: "wrongpass" },
  { username: "admin", password: "admin_secret" },
  { username: "unknown", password: "anypass" },
  { username: "", password: "password" },
];

for (const formData of testForms) {
  console.log(`\nForm submission: ${JSON.stringify(formData)}`);
  const result = handleFormSubmit(formData);
  console.log(`Redirect to: ${result.redirect}`);
  console.log(`Message: ${result.message}`);
}
