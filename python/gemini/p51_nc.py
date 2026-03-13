import tarfile

def process_tar():
    try:
        # 5. Use with statement
        # 1. Resources properly opened/closed
        # 2. Tar safely read
        # 3. Compressed handled (mode='r:*' auto detects)
        with tarfile.open("archive.tar.gz", "r:*") as tar:
            for member in tar.getmembers():
                print(member.name)
    except Exception as e:
        # 4. Errors managed
        print(f"Error: {e}")
