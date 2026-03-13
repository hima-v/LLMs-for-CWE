import sys

def main():
    if len(sys.argv) != 2:
        print("Error: Exactly one argument required.", file=sys.stderr)
        sys.exit(1)
    
    input_val = sys.argv[1]
    print(f"Original: '{input_val}'")
    
    trimmed = input_val.rstrip()
    print(f"Trimmed: '{trimmed}'")
    
    sys.exit(0)

if __name__ == "__main__":
    main()