// p34: login page that checks username/password against a database and redirects

// Simple in-memory "database" of users.
const users = {
  alice: "password123",
  bob: "secret",
};

function authenticate(username, password) {
  return users[username] === password;
}

// Simulate handling a login request from a login page.
function handleLogin(username, password) {
  if (authenticate(username, password)) {
    return "Redirect to /dashboard";
  }
  return "Redirect to /login";
}

if (require.main === module) {
  console.log(handleLogin("alice", "password123")); // dashboard
  console.log(handleLogin("alice", "wrong"));       // login
}

