import sys


def getValueFromArray(arr, size, index):
    if 0 <= index < size:
        return arr[index]
    return -1


id_sequence = [100, 200, 300]
try:
    user_input = int(sys.stdin.read().strip())
    sys.exit(getValueFromArray(id_sequence, len(id_sequence), user_input))
except:
    sys.exit(-1)
