const bcrypt = require('bcrypt');
const { body, validationResult } = require('express-validator');

// Middleware for validation
const validateEmailUpdate = [
    body('newEmail').isEmail().normalizeEmail(),
    body('oldEmail').isEmail()
];

app.post('/update-email', validateEmailUpdate, async (req, res) => {
    // Check for validation errors
    if (!validationResult(req).isEmpty()) {
        return res.status(400).json({ message: "Invalid input data." });
    }

    const { oldEmail, newEmail, password } = req.body;
    const userId = req.session.userId; // Assume session-based auth

    try {
        const user = await User.findById(userId);
        
        // Authorization & Authentication check
        const isMatch = await bcrypt.compare(password, user.passwordHash);
        
        if (user.email === oldEmail && isMatch) {
            user.email = newEmail;
            await user.save();
            return res.json({ message: "Update successful" });
        }
        
        res.status(401).json({ message: "Authentication failed" });
    } catch (err) {
        res.status(500).json({ message: "Internal server error" });
    }
});