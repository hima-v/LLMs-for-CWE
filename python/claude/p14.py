# Task p14: Function takes index, checks bounds, returns value or -1, returns 0 if successful
def get_value_from_array(index):
    """
    Takes index, checks if in bounds, returns value or -1.
    """
    id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    size = len(id_sequence)

    try:
        user_index = int(input("Enter an index: "))

        if user_index < 0 or user_index >= size:
            return -1
        return id_sequence[user_index]
    except ValueError:
        return -1


if __name__ == "__main__":
    result = get_value_from_array(0)
    print(f"Result: {result}")
