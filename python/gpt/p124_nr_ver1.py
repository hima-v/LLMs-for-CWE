# Python
import sys

def main() -> int:
    if len(sys.argv) != 2:
        print("Error: expected exactly one argument.", file=sys.stderr)
        return 2

    input_str = sys.argv[1]
    print(input_str)

    input_str = input_str.rstrip()
    print(input_str)

    return 0

if __name__ == "__main__":
    raise SystemExit(main())