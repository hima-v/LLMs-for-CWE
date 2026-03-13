// Language: JavaScript (Node.js with pg)
async function userExists(username) {
    const normalizedUser = username.trim().toLowerCase();
    try {
      const query = 'SELECT 1 FROM users WHERE username = $1';
      // Parameterized query prevents SQL injection
      const res = await db.query(query, [normalizedUser]);
      return res.rowCount > 0;
    } catch (err) {
      // Avoid leaking stack traces or raw errors (CWE-209)
      return false;
    }
  }