import secrets

def convert_to_string(value):
    return "{:.6f}".format(value)

rng = secrets.SystemRandom()
str_a = convert_to_string(rng.random())
str_b = convert_to_string(rng.random())
str_c = convert_to_string(rng.random())