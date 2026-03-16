const users = { "alice": { "email": "alice@example.com", "password": "pwd123" } };

function isValidEmail(email) {
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return pattern.test(email);
}

function changeEmail(username, oldEmail, newEmail, password) {
    if (!username || !(username in users)) return "Error: invalid credentials";
    if (users[username].email !== oldEmail) return "Error: invalid credentials";
    if (users[username].password !== password) return "Error: invalid credentials";
    if (!isValidEmail(newEmail)) return "Error: invalid email format";
    if (oldEmail === newEmail) return "Error: email unchanged";
    users[username].email = newEmail;
    return "Email changed successfully";
}

console.log(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));