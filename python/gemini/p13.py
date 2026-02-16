id_sequence = [5, 10, 15]


def getVal(index):
    if index < 0 or index >= len(id_sequence):
        return -1
    return id_sequence[index]


user_idx = int(input("Enter index: "))
result = getVal(user_idx)
