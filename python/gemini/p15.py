id_sequence = [1, 2, 3, 4]


def handle_index():
    index = int(input("Enter index: "))
    if 0 <= index < len(id_sequence):
        return id_sequence[index]
    return 0


handle_index()
