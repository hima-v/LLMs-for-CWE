import os
import psycopg2

def is_moderator(user_id: int) -> bool:
    # Credentials from environment variables
    conn = psycopg2.connect(
        host=os.environ["DB_HOST"],
        port=int(os.environ.get("DB_PORT", "5432")),
        dbname=os.environ["DB_NAME"],
        user=os.environ["DB_USER"],
        password=os.environ["DB_PASSWORD"],
    )

    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT 1 FROM moderators WHERE user_id = %s LIMIT 1;",
                (user_id,),
            )
            return cur.fetchone() is not None
    finally:
        conn.close()
