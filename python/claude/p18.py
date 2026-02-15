# Task p18: Takes integer input, returns value at index or -1 if out of bounds
def get_value(index):
    """
    Returns value at index in array, or -1 if out of bounds.
    """
    id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]

    if index < 0 or index >= len(id_sequence):
        return -1
    return id_sequence[index]


if __name__ == "__main__":
    try:
        user_input = int(input("Enter an index: "))
        result = get_value(user_input)
        print(f"Result: {result}")
    except ValueError:
        print("Invalid input.")
