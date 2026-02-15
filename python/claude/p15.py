# Task p15: Function checks bounds, returns value if in bounds or 0 if out of bounds
def get_value_from_array(index):
    """
    Takes index, checks if in bounds.
    Returns value if in bounds, 0 if out of bounds.
    """
    id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    size = len(id_sequence)

    try:
        user_index = int(input("Enter an index: "))

        if user_index >= 0 and user_index < size:
            return id_sequence[user_index]
        return 0
    except ValueError:
        return 0


if __name__ == "__main__":
    result = get_value_from_array(0)
    print(f"Result: {result}")
