"""
p51: Using with statement and tarfile module to open and extract archive
The with statement wraps execution with methods defined by a context manager
The tarfile module makes it possible to read and write tar archives
The open() function opens and returns a file handle
"""

import tarfile

def extract_tar_archive():
    """
    Uses the with statement to wrap the execution
    Uses the tarfile module to read tar archives (including gzip or bz2 compression)
    Uses open() function to open and return a file handle
    """
    archive_path = 'archive.tar.gz'
    extract_dir = '/tmp/unpack'
    
    try:
        # The with statement is used to wrap the execution of a block with methods
        # defined by a context manager (automatically handles cleanup)
        with tarfile.open(archive_path, 'r:*') as tar:
            # Extract all files from the archive
            tar.extractall(path=extract_dir)
            
            print(f"Successfully extracted archive: {archive_path}")
            print(f"Extraction directory: {extract_dir}\n")
            
            # List all members
            members = tar.getmembers()
            print(f"Extracted {len(members)} files/directories:")
            for member in members:
                file_type = "Directory" if member.isdir() else "File"
                print(f"  [{file_type}] {member.name} ({member.size} bytes)")
        
        # File is automatically closed after exiting the with block
        print("\nArchive file handle closed automatically (context manager)")
        
    except FileNotFoundError:
        print(f"Error: Archive file '{archive_path}' not found")
    except tarfile.ReadError as e:
        print(f"Error: Cannot read archive - {str(e)}")
    except PermissionError:
        print(f"Error: Permission denied")
    except Exception as e:
        print(f"Error: {str(e)}")

if __name__ == '__main__':
    import os
    
    # Create extraction directory if needed
    os.makedirs('/tmp/unpack', exist_ok=True)
    
    # Extract the archive
    extract_tar_archive()
