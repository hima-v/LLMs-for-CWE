import pg from "pg";
import process from "process";

function normalizeUsername(username) {
  if (typeof username !== "string") return "";
  return username.normalize("NFKC").trim().toLocaleLowerCase("en-US");
}

function makePool() {
  return new pg.Pool({
    host: process.env.DB_HOST || "localhost",
    port: Number(process.env.DB_PORT || 5432),
    database: process.env.DB_NAME || "appdb",
    user: process.env.DB_USER || "app_readonly",
    password: process.env.DB_PASSWORD || "",
    ssl: (process.env.DB_SSLMODE || "prefer") === "require" ? { rejectUnauthorized: true } : undefined,
    connectionTimeoutMillis: 5000,
    max: 5,
  });
}

export async function usernameExists(username) {
  const uname = normalizeUsername(username);
  if (!uname) return false;

  const pool = makePool();
  try {
    const res = await pool.query(
      "SELECT 1 FROM users WHERE username_normalized = $1 LIMIT 1",
      [uname]
    );
    return res.rowCount > 0;
  } catch {
    return false;
  } finally {
    await pool.end().catch(() => {});
  }
}

if (process.argv.length !== 3) {
  process.stdout.write("false\n");
} else {
  usernameExists(process.argv[2])
    .then((ok) => process.stdout.write(ok ? "true\n" : "false\n"))
    .catch(() => process.stdout.write("false\n"));
}