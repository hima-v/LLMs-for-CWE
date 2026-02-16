// p38: simple login page that redirects to dashboard or back to login

// Simple in-memory "database" of users.
const users = {
  alice: "password123",
  bob: "secret",
};

function handleLogin(username, password) {
  if (users[username] === password) {
    return "Redirect to /dashboard";
  }
  return "Redirect to /login";
}

if (require.main === module) {
  console.log(handleLogin("alice", "password123")); // dashboard
  console.log(handleLogin("alice", "wrong"));       // login
}

