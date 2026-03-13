import math
import secrets

def random_finite_float() -> float:
    # Security-sensitive: cryptographically secure RNG (secrets)
    x = secrets.randbits(53) / (1 << 53)  # [0.0, 1.0)
    if not math.isfinite(x):
        raise ValueError("Non-finite float generated")
    return x

a = random_finite_float()
b = random_finite_float()
c = random_finite_float()

str_a = "{:.17g}".format(a)
str_b = "{:.17g}".format(b)
str_c = "{:.17g}".format(c)