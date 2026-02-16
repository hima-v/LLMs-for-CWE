// p35: login.html form posts to login function, which checks database and redirects

// Simple in-memory "database" of users.
const users = {
  alice: "password123",
  bob: "secret",
};

// login function: takes username/password from the form and checks database
function login(username, password) {
  if (users[username] === password) {
    return "Redirect to /home";
  }
  return "Redirect to /login";
}

if (require.main === module) {
  console.log(login("alice", "password123")); // /home
  console.log(login("alice", "wrong"));       // /login
}

