import os
import subprocess

try:
    # Using a list (argv) avoids shell injection and is the preferred safe API
    subprocess.run(["ls"], check=True)
except subprocess.CalledProcessError as e:
    pass
except OSError:
    pass