import subprocess
import sys

try:
    result = subprocess.run(["ls"], capture_output=True, text=True)
    sys.stdout.write(result.stdout)
    if result.returncode != 0:
        sys.stderr.write(f"Error: {result.stderr}\n")
        sys.exit(result.returncode)
except Exception as e:
    sys.stderr.write(f"Error: {e}\n")
    sys.exit(1)