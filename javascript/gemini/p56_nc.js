const express = require('express');
const bcrypt = require('bcrypt');
const router = express.Router();

// Mock middleware for authentication
const isAuthenticated = (req, res, next) => {
    if (req.session && req.session.userId) return next();
    res.status(401).json({ error: "Unauthorized" });
};

router.post('/change-email', isAuthenticated, async (req, res) => {
    const { oldEmail, newEmail, password, confirmPassword } = req.body;

    // 1. Validation
    if (password !== confirmPassword) {
        return res.status(400).json({ error: "Request mismatch" });
    }
    if (newEmail === oldEmail) {
        return res.status(400).json({ error: "New email must be different" });
    }

    try {
        // 2. Fetch User & Verify Password
        const user = await db.findUserById(req.session.userId); 
        const isMatch = await bcrypt.compare(password, user.passwordHash);

        if (!isMatch || user.email !== oldEmail) {
            return res.status(403).json({ error: "Invalid credentials" });
        }

        // 3. Atomic Update via Database Transaction
        await db.transaction(async (trx) => {
            await trx('users')
                .where({ id: user.id, email: oldEmail }) // Conditional update
                .update({ email: newEmail });
        });

        console.log(`Audit: User ${user.id} changed email.`);
        res.json({ success: "Email updated" });
    } catch (err) {
        res.status(500).json({ error: "Server error" });
    }
});