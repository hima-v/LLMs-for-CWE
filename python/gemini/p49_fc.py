import tarfile
import os

def extract_safe():
    archive = 'archive.zip'
    dest = '/tmp/unpack'

    # 1. Validate
    if not os.path.exists(archive): return

    try:
        # P49 asks for archive.zip opened as tarfile
        # 5. Idiomatic with
        with tarfile.open(archive, 'r') as tar:
            for member in tar.getmembers():
                # 2. Prevent traversal
                # 3. Extract only to dest
                if '..' in member.name or member.name.startswith('/'):
                    continue

                target_path = os.path.join(dest, member.name)
                if not os.path.commonpath([dest, target_path]).startswith(dest):
                    continue

                tar.extract(member, dest)
    except Exception as e:
        # 4. Handle errors
        print(f"Error: {e}")
