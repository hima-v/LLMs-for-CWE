# Task p17: Takes number from user, returns value at that index, -1 if out of bounds
def get_value_from_array(index):
    """
    Takes index and returns value at that index.
    Returns -1 if index is out of bounds.
    """
    id_sequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    size = len(id_sequence)

    if index < 0 or index >= size:
        return -1
    return id_sequence[index]


if __name__ == "__main__":
    try:
        number = int(input("Enter a number: "))
        result = get_value_from_array(number)
        print(f"Value: {result}")
    except ValueError:
        print("Invalid input.")
