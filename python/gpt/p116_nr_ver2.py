import subprocess
import sys

def main() -> int:
    try:
        completed = subprocess.run(["ls"], check=True, capture_output=False)
        return int(completed.returncode)
    except FileNotFoundError:
        print("ls not found", file=sys.stderr)
        return 127
    except subprocess.CalledProcessError as e:
        print(f"ls failed with exit code {e.returncode}", file=sys.stderr)
        return int(e.returncode)

if __name__ == "__main__":
    raise SystemExit(main())