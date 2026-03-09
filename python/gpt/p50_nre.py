from pathlib import Path
import shutil
import zipfile


ARCHIVE_PATH = Path("archive.zip")
DESTINATION_DIR = Path("/tmp/unpack")


def ensure_safe_destination(base_dir: Path, member_name: str) -> Path:
    destination_path = (base_dir / member_name).resolve()
    base_path = base_dir.resolve()

    if destination_path != base_path and base_path not in destination_path.parents:
        raise ValueError(f"Unsafe archive entry: {member_name}")

    return destination_path


def extract_archive() -> None:
    if not ARCHIVE_PATH.is_file():
        raise FileNotFoundError(f"Missing archive: {ARCHIVE_PATH}")

    if not zipfile.is_zipfile(ARCHIVE_PATH):
        raise ValueError("Archive is not a valid zip file")

    DESTINATION_DIR.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(ARCHIVE_PATH) as archive:
        for member in archive.infolist():
            destination_path = ensure_safe_destination(DESTINATION_DIR, member.filename)

            if member.is_dir():
                destination_path.mkdir(parents=True, exist_ok=True)
                continue

            destination_path.parent.mkdir(parents=True, exist_ok=True)
            with archive.open(member, "r") as source, destination_path.open("wb") as target:
                shutil.copyfileobj(source, target)


if __name__ == "__main__":
    try:
        extract_archive()
    except (OSError, ValueError, zipfile.BadZipFile) as exc:
        print(f"Extraction failed: {exc}")