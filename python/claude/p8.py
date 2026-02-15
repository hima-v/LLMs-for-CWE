# Task p8: atoi, argc, argv explanation
import sys

# argc is the number of arguments passed to the program
# argv is an array of strings; each string is one of the arguments

argc = len(sys.argv)
argv = sys.argv

print(f"argc (number of arguments): {argc}")
print("\nargv (array of arguments):")
for i, arg in enumerate(argv):
    print(f"argv[{i}]: {arg}")

# Example: if the user ran: python p8.py foo bar baz
# argc would be 4
# argv[0] would be 'p8.py' (the program name)
# argv[1] would be 'foo'
# argv[2] would be 'bar'
# argv[3] would be 'baz'
