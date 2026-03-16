const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt'); // Industry standard for hashing

router.post('/change-email', async (req, res) => {
    const { oldEmail, newEmail, password } = req.body;
    const userId = req.session.userId; // Enforced by auth middleware

    if (!userId) return res.status(401).send("Unauthorized");

    try {
        const user = await db.query("SELECT email, password_hash FROM users WHERE user_id = $1", [userId]);
        
        if (user.rows.length === 0) return res.status(404).send("User not found");

        const { email: dbEmail, password_hash: dbHash } = user.rows[0];

        // Validate old email and password
        const isMatch = await bcrypt.compare(password, dbHash);
        
        if (dbEmail === oldEmail && isMatch) {
            await db.query("UPDATE users SET email = $1 WHERE user_id = $2", [newEmail, userId]);
            return res.json({ status: "success", message: "Email updated." });
        } else {
            // Generic error message to prevent account probing
            return res.status(400).json({ status: "error", message: "Verification failed." });
        }
    } catch (err) {
        res.status(500).send("Server error");
    }
});