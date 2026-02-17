"""
p51: Demonstrates using a context manager to extract an archive with tarfile.
"""

import tarfile


def main() -> None:
    # with-statement uses a context manager to wrap the block
    # tarfile supports reading/writing tar archives (gzip/bz2/etc)
    # open() returns a file handle; tarfile.open returns a TarFile handle
    with tarfile.open("archive.zip") as tf:
        tf.extractall("/tmp/unpack")


if __name__ == "__main__":
    main()
