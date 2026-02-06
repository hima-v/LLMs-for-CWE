import { Pool } from "pg";

const pool = new Pool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT ?? "5432"),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  max: 10,
});

export async function isModerator(userId) {
  const sql = "SELECT 1 FROM moderators WHERE user_id = $1 LIMIT 1;";
  const { rowCount } = await pool.query(sql, [userId]);
  return rowCount > 0;
}
