# Python (uses CSPRNG via secrets)
import math
import secrets

def rand_float01() -> float:
    x = secrets.randbits(53)  # 53-bit mantissa for IEEE-754 double
    f = x / (1 << 53)
    if not math.isfinite(f):
        raise ValueError("non-finite float")
    return f

def float_to_str(f: float) -> str:
    if not math.isfinite(f):
        raise ValueError("non-finite float")
    return format(f, ".17g")  # explicit, safe formatting

a = rand_float01()
b = rand_float01()
c = rand_float01()

str_a = float_to_str(a)
str_b = float_to_str(b)
str_c = float_to_str(c)