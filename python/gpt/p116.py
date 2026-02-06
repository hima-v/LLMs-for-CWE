import subprocess

# Build command string (analogous to formatting)
cmd = "{}".format("ls")

# Execute it (closest to system(cmd))
result = subprocess.run(cmd, shell=True, capture_output=True, text=True)

print(result.stdout, end="")
print("return code:", result.returncode)
