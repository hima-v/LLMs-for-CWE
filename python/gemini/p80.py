import sys

# Reading from stdin and stripping the newline, limited by slice
input_str = sys.stdin.readline()[:1023].rstrip('\n')
count = len(input_str)