id_sequence = [1, 2, 3, 4]


def check_and_return():
    index = int(input("Enter index: "))
    if index < 0 or index >= len(id_sequence):
        return -1
    return id_sequence[index]


check_and_return()
sys.exit(0)
