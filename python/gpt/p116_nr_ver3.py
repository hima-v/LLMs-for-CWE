import subprocess
import sys

def main() -> int:
    cmd = "ls"
    print(f"Executing: {cmd}")

    try:
        subprocess.run(["ls"], check=True)
        return 0
    except FileNotFoundError:
        print("ls not found", file=sys.stderr)
        return 127
    except subprocess.CalledProcessError as e:
        print(f"ls failed with exit code {e.returncode}", file=sys.stderr)
        return int(e.returncode)

if __name__ == "__main__":
    raise SystemExit(main())