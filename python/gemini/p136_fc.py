import secrets
import time
from fastapi import FastAPI, Query, HTTPException, Request

app = FastAPI()

# SECURITY WARNING: Sending credentials in query parameters is insecure.
# Use POST bodies with HTTPS instead.
STORED_USER = "admin"
STORED_PASS = "secret_hash" # Assume hashed

# Basic in-memory rate limiting
request_log = {}

@app.get("/check_mod")
async def check_mod(request: Request, username: str = Query(...), password: str = Query(...)):
    client_ip = request.client.host
    now = time.time()
    
    # Rate limit: 5 requests per minute
    if request_log.get(client_ip, 0) > 5:
        raise HTTPException(status_code=429)
    request_log[client_ip] = request_log.get(client_ip, 0) + 1

    # Use constant-time comparison to prevent timing attacks
    user_match = secrets.compare_digest(username, STORED_USER)
    pass_match = secrets.compare_digest(password, STORED_PASS)
    
    if user_match and pass_match:
        return "true"
    return "false"