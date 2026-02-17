import subprocess
import sys

if len(sys.argv) < 2:
    print("Usage: python script.py <username>")
    sys.exit(1)

username = sys.argv[1]

# run() returns a CompletedProcess instance
result = subprocess.run(["id", username])

print(f"Exit status: {result.returncode}")