import secrets

def float_to_str(val):
    # secrets.SystemRandom() is cryptographically secure
    # formatting with 'f' ensures finite, fixed-point representation
    return "{:.10f}".format(val)

rng = secrets.SystemRandom()

str_a = float_to_str(rng.uniform(0, 100))
str_b = float_to_str(rng.uniform(0, 100))
str_c = float_to_str(rng.uniform(0, 100))