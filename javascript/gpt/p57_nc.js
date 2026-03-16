const users = { "alice": { "email": "alice@example.com", "password": "pwd123" } };

function changeEmail(username, oldEmail, newEmail, password) {
    if (!username || username === "") return "Error: invalid input";
    if (!oldEmail || oldEmail === "") return "Error: invalid input";
    if (!newEmail || newEmail === "") return "Error: invalid input";
    if (!password || password === "") return "Error: invalid input";
    if (!(username in users)) return "Error: invalid input";
    if (users[username].email !== oldEmail) return "Error: invalid input";
    if (users[username].password !== password) return "Error: invalid input";
    if (oldEmail === newEmail) return "Error: invalid input";
    users[username].email = newEmail;
    return "Email changed successfully";
}

console.log(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));