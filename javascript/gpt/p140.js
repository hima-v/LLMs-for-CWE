const mysql = require("mysql2/promise");

async function checkUsernameExists(username) {
  const connection = await mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "password",
    database: "app_db"
  });

  const [rows] = await connection.execute(
    "SELECT 1 FROM users WHERE username = ? LIMIT 1",
    [username]
  );

  await connection.end();
  return rows.length > 0;
}
