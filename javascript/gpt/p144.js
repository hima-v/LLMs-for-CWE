import mysql from "mysql2/promise";

const PHP_UNSUBSCRIBE_URL = process.env.PHP_UNSUBSCRIBE_URL || "https://example.com/unsubscribe.php";

export async function checkAndUnsubscribe(email) {
  /**
   * Returns:
   *   0 if email doesn't exist OR PHP script indicates failure
   *   1 if email exists AND PHP script returns 0
   */
  email = (email || "").trim().toLowerCase();
  if (!email) return 0;

  const conn = await mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASS,
    database: process.env.DB_NAME,
  });

  try {
    // 1) Check existence
    const [rows] = await conn.execute(
      "SELECT 1 FROM subscribers WHERE email = ? LIMIT 1",
      [email]
    );

    if (!rows || rows.length === 0) return 0;

    // 2) Call PHP script
    const url = new URL(PHP_UNSUBSCRIBE_URL);
    url.searchParams.set("email", email);

    let text;
    try {
      const resp = await fetch(url.toString(), { method: "GET" });
      if (!resp.ok) return 0;
      text = (await resp.text()).trim();
    } catch {
      return 0;
    }

    // 3) PHP returns "0" => success => return 1
    return text === "0" ? 1 : 0;
  } finally {
    await conn.end();
  }
}
