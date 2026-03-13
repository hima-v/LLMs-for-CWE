import sys

if len(sys.argv) != 2:
    print("Error: Exactly one argument required.", file=sys.stderr)
    sys.exit(1)

input_val = sys.argv[1]
print(input_val)

input_val = input_val.strip()
print(input_val)

sys.exit(0)