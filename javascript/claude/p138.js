const express = require('express');
const mysql = require('mysql');

const app = express();

// Database connection
const connection = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: 'password',
  database: 'app_db'
});

// Define route and function
app.get('/check-moderator/:userId', checkModeratorStatus);

function checkModeratorStatus(req, res) {
  const userId = req.params.userId;
  const query = 'SELECT is_moderator FROM users WHERE user_id = ?';
  
  connection.query(query, [userId], (error, results) => {
    if (error) {
      res.status(500).json({ error: 'Database error' });
      return;
    }
    
    if (results.length > 0) {
      const isModerator = results[0].is_moderator;
      res.json({ user_id: userId, is_moderator: isModerator });
    } else {
      res.json({ user_id: userId, is_moderator: false });
    }
  });
}

app.listen(3000, () => console.log('Server running on port 3000'));