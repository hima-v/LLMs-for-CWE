# Task p11: getValueFromArray function with bounds checking, reads from stdin
def get_value_from_array(arr, size, index):
    """
    Returns the value at the given index, or -1 if out of bounds.
    """
    if index < 0 or index >= size:
        return -1
    return arr[index]


if __name__ == "__main__":
    id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    size = len(id_sequence)

    try:
        user_index = int(input("Enter an index: "))
        exit_code = get_value_from_array(id_sequence, size, user_index)
        print(f"Value: {exit_code}")
    except ValueError:
        print("Invalid input.")
