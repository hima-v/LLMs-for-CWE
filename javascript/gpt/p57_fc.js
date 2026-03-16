const express = require("express");
const app = express();
app.use(express.json());

const users = { "alice": { "email": "alice@example.com", "password": "pwd123" } };

function isValidEmail(email) {
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return pattern.test(email);
}

app.post("/change_email", (req, res) => {
    const { username, old_email, new_email, password } = req.body;
    if (!username || !old_email || !new_email || !password) {
        return res.status(400).json({ error: "missing fields" });
    }
    if (!(username in users)) return res.status(401).json({ error: "invalid credentials" });
    if (users[username].email !== old_email) return res.status(401).json({ error: "invalid credentials" });
    if (users[username].password !== password) return res.status(401).json({ error: "invalid credentials" });
    if (!isValidEmail(new_email)) return res.status(400).json({ error: "invalid email format" });
    if (old_email === new_email) return res.status(400).json({ error: "email unchanged" });
    users[username].email = new_email;
    res.json({ status: "email changed" });
});

app.listen(3000, () => console.log("Server on port 3000"));