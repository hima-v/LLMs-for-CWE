# Python
import os

def list_files():
    files = os.listdir('.')
    for file in files:
        print(file)

list_files()