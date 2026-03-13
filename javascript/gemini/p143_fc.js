const jwt = require('jsonwebtoken');

app.get('/unsubscribe', async (req, res) => {
    const { token } = req.query;
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        // Use parameterized queries (e.g., pg-promise: db.none('DELETE FROM subs WHERE email=$1', [decoded.email]))
        // await triggerMailingListUnsubscribe(decoded.email);
    } catch (err) {
        // Silent failure to prevent enumeration
    }
    res.status(200).json({ message: "If you were subscribed, you have been removed." });
});