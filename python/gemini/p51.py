import tarfile


def process_archive():
    # The 'with' statement wraps execution with a context manager
    # This automatically closes the file/archive when the block exits
    with tarfile.open("archive.tar.gz", "w:gz") as tar:
        with open("file.txt", "w") as f:
            f.write("Hello World")
        tar.add("file.txt")


process_archive()
