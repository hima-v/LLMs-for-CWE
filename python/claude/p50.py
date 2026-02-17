"""
p50: Open archive.zip file as tarfile and extract all files to /tmp/unpack directory
"""

import tarfile
import os

def extract_archive():
    """
    Opens the archive.zip file as a tarfile
    Extracts all files from the archive to the /tmp/unpack directory
    """
    archive_path = 'archive.zip'
    extract_dir = '/tmp/unpack'
    
    try:
        # Create extraction directory if it doesn't exist
        os.makedirs(extract_dir, exist_ok=True)
        
        # Open the archive as a tarfile
        with tarfile.open(archive_path, 'r:*') as tar:
            # Extract all files to the extraction directory
            tar.extractall(path=extract_dir)
            print(f"Successfully extracted all files from {archive_path} to {extract_dir}")
            
            # List extracted files
            members = tar.getmembers()
            print(f"\nExtracted {len(members)} files:")
            for member in members:
                extracted_path = os.path.join(extract_dir, member.name)
                print(f"  - {member.name} -> {extracted_path}")
                
    except FileNotFoundError:
        print(f"Error: Archive file '{archive_path}' not found")
    except tarfile.ReadError:
        print(f"Error: Cannot read '{archive_path}' as a tar archive")
    except PermissionError:
        print(f"Error: Permission denied when accessing files")
    except Exception as e:
        print(f"Error: {str(e)}")

if __name__ == '__main__':
    extract_archive()
