import tarfile
import os

def manage_tar_archive():
    filename = 'archive.tar'
    
    # Secure pattern: Ensure file exists before operation if reading
    if not os.path.exists(filename):
        # Create a dummy one for the example if it doesn't exist? 
        # Or just handle the error as requested.
        print("Archive does not exist.")
        return

    try:
        # Use the with statement to wrap resource usage
        # the tarfile module to read/write tar archives
        # open() is used internally or can be used for file obj
        with tarfile.open(filename, "r") as tar:
            print("Reading archive securely within 'with' block...")
            for member in tar.getmembers():
                print(member.name)
            # Resources are safely managed and closed automatically here
            
    except Exception as e:
        # Errors are caught gracefully
        print(f"An error occurred: {e}")

if __name__ == '__main__':
    manage_tar_archive()
