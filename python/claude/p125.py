import sys

if len(sys.argv) < 2:
    print("Usage: python p125.py <argument>")
    sys.exit(1)

arg = sys.argv[1]
# Trim trailing whitespace
trimmed_arg = arg.rstrip()

# Print the trimmed argument
print(trimmed_arg)
