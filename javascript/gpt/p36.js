// p36: login function checks if username exists, then verifies password

// Simple in-memory "database" of users.
const users = {
  alice: "password123",
  bob: "secret",
};

function login(username, password) {
  // Check if username exists.
  if (!Object.prototype.hasOwnProperty.call(users, username)) {
    console.log("Username not found. Return.");
    return;
  }

  // Username exists; check password.
  if (users[username] === password) {
    console.log("Password correct. Redirect to /home");
    return "/home";
  } else {
    console.log("Password incorrect. Return.");
    return;
  }
}

if (require.main === module) {
  login("alice", "password123");
  login("alice", "wrong");
  login("charlie", "whatever");
}

