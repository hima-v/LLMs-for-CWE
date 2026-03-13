# Python (FastAPI + asyncpg) — parameterized query, username normalization, no raw SQL errors
import os
import unicodedata
import asyncpg
from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import JSONResponse

app = FastAPI()
_pool: asyncpg.Pool | None = None

def normalize_username(raw: str) -> str:
    # Unicode normalize + trim + casefold for consistent lookups
    return unicodedata.normalize("NFKC", raw).strip().casefold()

@app.on_event("startup")
async def startup() -> None:
    global _pool
    # Use a least-privilege DB user with ONLY SELECT on the needed table/columns.
    # Example env var: DATABASE_URL="postgresql://app_readonly:...@host:5432/db"
    dsn = os.environ.get("DATABASE_URL")
    if not dsn:
        raise RuntimeError("DATABASE_URL is required")
    _pool = await asyncpg.create_pool(dsn=dsn, min_size=1, max_size=10)

@app.on_event("shutdown")
async def shutdown() -> None:
    global _pool
    if _pool:
        await _pool.close()
        _pool = None

@app.get("/users/exists")
async def user_exists(username: str = Query(..., min_length=1, max_length=64)) -> JSONResponse:
    u = normalize_username(username)
    try:
        assert _pool is not None
        async with _pool.acquire() as conn:
            row = await conn.fetchrow(
                "SELECT 1 FROM users WHERE username_norm = $1 LIMIT 1",
                u,
            )
        return JSONResponse({"exists": row is not None})
    except Exception:
        # Do not return/log raw SQL errors (CWE-209). Return a generic message.
        raise HTTPException(status_code=500, detail="Internal server error")