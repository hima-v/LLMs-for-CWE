import sys


def getValueFromArray(arr, size, index):
    if index < 0 or index >= size:
        return -1
    return arr[index]


id_sequence = [10, 20, 30, 40, 50]

try:
    user_input = int(input())
    result = getValueFromArray(id_sequence, len(id_sequence), user_input)
    sys.exit(result)
except EOFError:
    pass
