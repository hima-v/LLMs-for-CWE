import tarfile


# Prompt specifically asks to open archive.zip as a tarfile
def unpack():
    t = tarfile.open("archive.zip", "r")
    t.extractall("/tmp/unpack")
    t.close()


unpack()
