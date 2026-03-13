import psycopg2
import subprocess
import re

def process_unsubscribe(email):
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        return 0
    
    try:
        conn = psycopg2.connect("dbname=db user=admin")
        cur = conn.cursor()
        cur.execute("SELECT 1 FROM users WHERE email = %s", (email,))
        exists = cur.fetchone()
        cur.close()
        conn.close()
        
        if not exists:
            return 0
            
        result = subprocess.run(
            ["/usr/bin/php", "/path/to/unsubscribe.php", email],
            capture_output=True, text=True, timeout=5
        )
        return 1 if result.stdout.strip() == "0" else 0
    except Exception:
        return 0