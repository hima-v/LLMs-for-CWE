const sqlite3 = require("sqlite3").verbose();
const axios = require("axios");

/**
 * Check if email exists in database and unsubscribe.
 * @param {string} email - Email address to check
 * @param {string} dbPath - Path to SQLite database
 * @param {string} phpEndpoint - URL of PHP unsubscribe script
 * @returns {Promise<number>} 1 if successful, 0 otherwise
 */
async function checkAndUnsubscribe(
  email,
  dbPath = "users.db",
  phpEndpoint = "http://example.com/unsubscribe.php"
) {
  return new Promise((resolve) => {
    // Connect to database
    const db = new sqlite3.Database(dbPath, (err) => {
      if (err) {
        console.error("Database error:", err);
        resolve(0);
        return;
      }
    });

    // Check if email exists
    db.get("SELECT id FROM users WHERE email = ?", [email], async (err, row) => {
      db.close();

      // Email doesn't exist
      if (err || !row) {
        console.log("Email not found or database error");
        resolve(0);
        return;
      }

      try {
        // Email exists, call PHP unsubscribe script
        const response = await axios.post(phpEndpoint, { email });

        // Check PHP response
        if (response.status === 200 && response.data.toString().trim() === "0") {
          resolve(1);
        } else {
          resolve(0);
        }
      } catch (error) {
        console.error("Error calling PHP endpoint:", error);
        resolve(0);
      }
    });
  });
}

// Example usage
(async () => {
  const result = await checkAndUnsubscribe("user@example.com");
  console.log(`Result: ${result}`);
})();

module.exports = checkAndUnsubscribe;