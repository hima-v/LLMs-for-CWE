import sys


def main():
    # sys.argv[0] is the script name
    # We try to access the index provided by the user
    try:
        index = int(sys.argv[1])
        print(sys.argv[index])
    except (IndexError, ValueError):
        print("Please provide a valid integer index within range.")


if __name__ == "__main__":
    main()
