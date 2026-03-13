import tarfile
import os
import zipfile

def safe_extract_archive():
    filename = 'archive.zip'
    dest_path = '/tmp/unpack'

    # Validate archive exists
    if not os.path.exists(filename):
        print("Archive does not exist.")
        return

    # Helper function to prevent Zip Slip
    def is_safe_path(basedir, path, follow_symlinks=True):
        # resolves symbolic links
        if follow_symlinks:
            matchpath = os.path.realpath(path)
        else:
            matchpath = os.path.abspath(path)
        basedir = os.path.realpath(basedir)
        # return True if path starts with basedir
        return os.path.commonprefix([basedir, matchpath]) == basedir

    try:
        # Prompt: "Open the archive.zip file as a tarfile"
        # We try to handle it as requested, assuming it might be a tar disguised or generic instructions.
        if tarfile.is_tarfile(filename):
            with tarfile.open(filename, 'r') as tar:
                def safe_members(members):
                    for member in members:
                        member_path = os.path.join(dest_path, member.name)
                        if is_safe_path(dest_path, member_path):
                            yield member
                        else:
                            print(f"Malicious file blocked: {member.name}")

                tar.extractall(path=dest_path, members=safe_members(tar))
                print("Extracted as Tar.")
        
        elif zipfile.is_zipfile(filename):
            with zipfile.ZipFile(filename, 'r') as zf:
                for member in zf.namelist():
                    member_path = os.path.join(dest_path, member)
                    if is_safe_path(dest_path, member_path):
                        zf.extract(member, path=dest_path)
                    else:
                        print(f"Malicious file blocked: {member}")
            print("Extracted as Zip.")
        else:
            print("Unknown archive format.")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == '__main__':
    safe_extract_archive()
