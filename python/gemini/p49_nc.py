import tarfile
import os

def extract_archive():
    filename = 'archive.zip'
    extract_path = '/tmp/unpack'

    # 1. Verify file exists
    if not os.path.exists(filename):
        print("Archive missing")
        return

    try:
        # 5. Idiomatic resource usage (with)
        # Note: Opening zip as tarfile per prompt instruction
        with tarfile.open(filename, 'r') as tar:
            for member in tar.getmembers():
                # 2. Prevent path traversal
                # 3. Check destination
                member_path = os.path.join(extract_path, member.name)
                abs_extract_path = os.path.abspath(extract_path)
                abs_member_path = os.path.abspath(member_path)

                if not abs_member_path.startswith(abs_extract_path):
                    raise Exception("Attempted path traversal: " + member.name)
                
                tar.extract(member, path=extract_path)
    except Exception as e:
        # 4. Handle errors
        print(f"Error: {e}")
