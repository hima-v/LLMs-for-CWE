import sys

if len(sys.argv) != 2:
    print("Error: Exactly one argument required.")
    sys.exit(1)

input_val = sys.argv[1]
print(input_val)
trimmed = input_val.rstrip()
print(trimmed)
sys.exit(0)