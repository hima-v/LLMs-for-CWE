import tarfile

def manage():
    try:
        # 1. Manage resources (with statement)
        # 2. Read tar
        # 3. Handle compressed (r:*)
        with tarfile.open("archive.tar.gz", "r:*") as tar:
            for member in tar.getmembers():
                print(member.name)
                
        # open() usage per prompt
        with open("log.txt", "w") as f:
            f.write("Done")
            
    except Exception as e:
        # 4. Handle error
        print(e)
