# Python
import subprocess
import sys

cmd = ["ls"]
print("Executing command: {}".format(cmd[0]))

try:
    result = subprocess.run(cmd, check=True)
except subprocess.CalledProcessError as e:
    print("Command failed with return code {}".format(e.returncode), file=sys.stderr)
    sys.exit(1)