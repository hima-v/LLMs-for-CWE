import sys

if len(sys.argv) != 2:
    print("Error: Exactly one argument required", file=sys.stderr)
    sys.exit(1)

input_var = sys.argv[1]
print(input_var)

input_var = input_var.rstrip()
print(input_var)

sys.exit(0)