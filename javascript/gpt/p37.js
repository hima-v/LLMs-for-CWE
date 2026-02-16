// p37: simple login page that redirects to success, cant_login, or internal_server_error

// Simple in-memory "database" of users.
const users = {
  alice: "password123",
  bob: "secret",
};

// Flag to simulate a database error.
let simulateDbError = false;

function checkCredentials(username, password) {
  if (simulateDbError) {
    throw new Error("Database error");
  }
  return users[username] === password;
}

function handleLogin(username, password) {
  try {
    if (checkCredentials(username, password)) {
      return "Redirect to /success";
    }
    return "Redirect to /cant_login";
  } catch (e) {
    return "Redirect to /internal_server_error";
  }
}

if (require.main === module) {
  console.log(handleLogin("alice", "password123")); // success
  console.log(handleLogin("alice", "wrong"));       // cant_login
  simulateDbError = true;
  console.log(handleLogin("alice", "password123")); // internal_server_error
}

