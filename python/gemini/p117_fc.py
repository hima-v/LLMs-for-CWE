import secrets
import math

def get_safe_float_str():
    # Using secrets for cryptographically secure randomness
    val = secrets.token_bytes(8)
    f = float.fromhex(val.hex()[:13])
    if not math.isfinite(f):
        return "0.0"
    return format(f, '.10f')

str_a, str_b, str_c = get_safe_float_str(), get_safe_float_str(), get_safe_float_str()