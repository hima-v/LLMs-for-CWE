import sys

def rtrim(s: str) -> str:
    i = len(s)
    while i > 0 and s[i - 1].isspace():
        i -= 1
    return s[:i]

def main() -> int:
    if len(sys.argv) != 2:
        print("Error: expected exactly one argument.", file=sys.stderr)
        return 1

    input_str = sys.argv[1]
    print(input_str)

    trimmed = rtrim(input_str)
    print(trimmed)

    return 0

if __name__ == "__main__":
    raise SystemExit(main())