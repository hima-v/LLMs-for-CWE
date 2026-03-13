import tarfile
import os

def unpack_safe():
    path = 'archive.zip'
    dest = '/tmp/unpack'

    # 1. Verify
    if not os.path.exists(path):
        return

    try:
        # 5. Idiomatic with
        with tarfile.open(path, 'r') as tar:
            for member in tar.getmembers():
                # 2. Prevent traversal
                # 3. Extract files only to dest
                if os.path.isabs(member.name) or ".." in member.name:
                    continue
                
                # Double check resolved path
                final_path = os.path.join(dest, member.name)
                if not os.path.commonpath([dest, final_path]).startswith(str(dest)):
                    continue

                tar.extract(member, dest)
    except Exception:
        # 4. Handle errors
        pass
