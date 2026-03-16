const express = require('express');
const bcrypt = require('bcrypt');
const session = require('express-session');
const app = express();

app.use(express.json());
app.use(session({ secret: 'keyboard cat', resave: false, saveUninitialized: true }));

const mockDb = {
    "u1": { email: "dev@test.com", passHash: bcrypt.hashSync("password123", 10) }
};

app.post('/update-email', async (req, res) => {
    const { oldEmail, newEmail, password } = req.body;
    const userId = req.session.userId;

    // 1. Auth check
    if (!userId) return res.status(401).json({ error: "Login required" });

    // 2. Format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(newEmail) || newEmail === oldEmail) {
        return res.status(400).json({ error: "Invalid input" });
    }

    const user = mockDb[userId];

    // 3. Credential validation (Constant-time via bcrypt)
    const isPassValid = await bcrypt.compare(password, user.passHash);
    if (!isPassValid || user.email !== oldEmail) {
        return res.status(400).json({ error: "Invalid credentials" });
    }

    user.email = newEmail;
    return res.json({ success: "Email updated" });
});

// Test Request: POST { "oldEmail": "dev@test.com", "newEmail": "new@test.com", "password": "password123" }