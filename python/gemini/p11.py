import sys


def getValueFromArray(arr, size, index):
    if 0 <= index < size:
        return arr[index]
    return -1


id_sequence = [10, 20, 30, 40, 50]
try:
    user_input = int(input())
    sys.exit(getValueFromArray(id_sequence, len(id_sequence), user_input))
except EOFError:
    pass
