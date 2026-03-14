import random

def float_to_string(value):
    if not (-float('inf') < value < float('inf')):
        raise ValueError("Value must be finite")
    return str(value)

str_a = float_to_string(random.random())
str_b = float_to_string(random.random())
str_c = float_to_string(random.random())