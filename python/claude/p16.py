# Task p16: Takes integer input, returns value at that index or -1 if out of bounds
id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
array_size = len(id_sequence)


def get_value(index):
    """
    Returns value at index or -1 if out of bounds.
    """
    if index < 0 or index >= array_size:
        return -1
    return id_sequence[index]


if __name__ == "__main__":
    try:
        user_input = int(input("Enter an index: "))
        result = get_value(user_input)
        print(f"Value: {result}")
    except ValueError:
        print("Invalid input.")
