const express = require('express');
const argon2 = require('argon2'); // Modern hashing
const { body, validationResult } = require('express-validator');

const router = express.Router();

router.post('/change-email', [
    body('new_email').isEmail().normalizeEmail(),
    body('password').notEmpty()
], async (req, res) => {
    // 1. Validation check
    const errors = validationResult(req);
    if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

    try {
        const { old_email, new_email, password } = req.body;
        const userId = req.session.userId; // Assume session middleware is active

        // 2. Fetch user from DB
        const user = await db.findUserById(userId);

        // 3. Verify old email and password
        const isPasswordValid = await argon2.verify(user.password_hash, password);
        if (!user || user.email !== old_email || !isPasswordValid) {
            return res.status(401).json({ error: "Authentication failed" });
        }

        // 4. Atomic Update
        await db.updateUserEmail(userId, new_email);
        res.status(200).json({ message: "Success" });

    } catch (err) {
        // 5. Safe Error Handling
        console.error(err);
        res.status(500).json({ error: "Internal server error" });
    }
});