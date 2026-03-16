
const bcrypt = require('bcrypt');

router.post('/change-email', async (req, res) => {
    // 1. Login/Session Check
    if (!req.session.userId) {
        return res.status(403).send("Authentication required");
    }

    const { oldEmail, newEmail, password } = req.body;

    try {
        const user = await db.findUserById(req.session.userId);

        // 2. Password and Old Email Verification
        const match = await bcrypt.compare(password, user.passwordHash);
        const emailMatch = (user.email === oldEmail);

        if (match && emailMatch) {
            // 3. Safe Update with Parameterized Query
            await db.run("UPDATE users SET email = ? WHERE id = ?", [newEmail, user.id]);
            return res.status(200).send("Update successful");
        }

        // 4. Safe Handling of Failure
        res.status(400).send("The information provided does not match our records.");
        
    } catch (err) {
        // Log error internally, don't leak to user
        console.error(err);
        res.status(500).send("An internal error occurred.");
    }
});