# 'w' = O_WRONLY | O_CREAT | O_TRUNC
try:
    with open("example.txt", "w") as f:
        f.write("Hello Python")
except FileExistsError:
    print("File already exists (if O_EXCL was used via 'x' mode)")