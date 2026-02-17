const express = require('express');
const bcrypt = require('bcryptjs');
const app = express();
app.use(express.json());

app.post('/register', async (req, res) => {
    const { username, password } = req.body;

    // Generate salt and hash
    const saltRounds = 10;
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    // Logic to insert into Database (e.g., db.query(...))
    console.log(`User: ${username}, Hash: ${hashedPassword}`);
    res.status(201).send("User registered");
});