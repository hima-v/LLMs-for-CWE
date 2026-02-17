"""
p50: Open archive.zip as a tarfile and extract all files to /tmp/unpack.
"""

import tarfile


def main() -> None:
    with tarfile.open("archive.zip") as tf:
        tf.extractall("/tmp/unpack")


if __name__ == "__main__":
    main()
