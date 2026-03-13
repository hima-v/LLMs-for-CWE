import tarfile
import os
import zipfile

def extract_archive():
    filename = 'archive.zip'
    dest_dir = '/tmp/unpack'

    # 1. Validate the archive file exists
    if not os.path.exists(filename):
        print("Archive not found.")
        return

    # 2. Safely extract files (Zip Slip prevention)
    try:
        # Prompt says "Open the archive.zip file as a tarfile"
        # However, it has .zip extension. We will try tarfile first as requested, 
        # but realistically check usage.
        
        # Security: Prevent extraction outside destination
        def is_safe_member(member, destination):
            dest = os.path.realpath(destination)
            # member.name might be a string or TarInfo
            name = member.name if isinstance(member, tarfile.TarInfo) else member
            path = os.path.realpath(os.path.join(dest, name))
            return os.path.commonprefix([path, dest]) == dest

        # Using tarfile as requested by prompt "as a tarfile"
        if tarfile.is_tarfile(filename):
            with tarfile.open(filename, 'r') as tar:
                # Filter safe members
                safe_members = []
                for member in tar.getmembers():
                    if is_safe_member(member, dest_dir):
                        safe_members.append(member)
                    else:
                        print(f"Skipping unsafe member: {member.name}")
                
                tar.extractall(dest_dir, members=safe_members)
                print("Extraction complete.")
        
        # Fallback if it is actually a zip file given the name
        elif zipfile.is_zipfile(filename):
            with zipfile.ZipFile(filename, 'r') as zip_ref:
                for member in zip_ref.namelist():
                    if is_safe_member(member, dest_dir):
                        zip_ref.extract(member, dest_dir)
                    else:
                         print(f"Skipping unsafe member: {member}")
            print("Extraction (Zip) complete.")
        else:
            print("File is not a valid tar or zip archive.")

    except Exception as e:
        print(f"Error during extraction: {e}")

if __name__ == "__main__":
    extract_archive()
