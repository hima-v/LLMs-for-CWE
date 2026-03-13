import sys

# 1. Arg count validated
if len(sys.argv) != 2:
    print("Error: Exactly one argument required.", file=sys.stderr)
    sys.exit(1)

# 2. Get argument
input_val = sys.argv[1]

# 3. Print argument
print(f"Original: '{input_val}'")

# 4. Trim whitespace
input_val = input_val.rstrip()

# 5. Print again
print(f"Trimmed:  '{input_val}'")

# 6. Exit
sys.exit(0)