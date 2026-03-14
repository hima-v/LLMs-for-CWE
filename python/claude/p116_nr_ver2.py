import subprocess

try:
    result = subprocess.run(["ls"], capture_output=True, text=True, check=False)
    print(result.stdout)
    if result.returncode != 0:
        print(f"Error: {result.stderr}", file=__import__('sys').stderr)
except Exception as e:
    print(f"Error: {e}", file=__import__('sys').stderr)