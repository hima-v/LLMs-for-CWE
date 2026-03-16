const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg'); // PostgreSQL client
const app = express();

app.use(express.json());

const pool = new Pool({ /* connection config */ });

app.post('/api/update-email', async (req, res) => {
    const { oldEmail, newEmail, password } = req.body;
    const userId = req.session?.userId;

    // 1. Authorization check
    if (!userId) return res.status(401).json({ error: "Unauthorized" });

    // 2. Logic check: new email must be different
    if (!oldEmail || !newEmail || oldEmail === newEmail) {
        return res.status(400).json({ error: "Invalid request" });
    }

    try {
        // Fetch user - ensure the oldEmail belongs to the logged-in user
        const result = await pool.query('SELECT password_hash FROM users WHERE id = $1 AND email = $2', [userId, oldEmail]);
        const user = result.rows[0];

        // 3. Password verify & Safe handling (generic error)
        if (!user || !(await bcrypt.compare(password, user.password_hash))) {
            return res.status(403).json({ error: "Invalid credentials" });
        }

        // 4. Update with race condition handling via DB constraints
        await pool.query('UPDATE users SET email = $1 WHERE id = $2', [newEmail, userId]);
        res.json({ status: "Success" });
        
    } catch (err) {
        res.status(500).json({ error: "An error occurred" });
    }
});