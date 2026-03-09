from pathlib import Path
import shutil
import zipfile


ARCHIVE_PATH = Path("archive.zip")
DESTINATION_DIR = Path("/tmp/unpack")


def validate_archive_path(archive_path: Path) -> None:
    if not archive_path.is_file():
        raise FileNotFoundError(f"Missing archive: {archive_path}")

    if not zipfile.is_zipfile(archive_path):
        raise ValueError("Archive is not a valid zip file")


def safe_destination_path(base_dir: Path, member_name: str) -> Path:
    resolved_base_dir = base_dir.resolve()
    resolved_target = (resolved_base_dir / member_name).resolve()
    if resolved_target != resolved_base_dir and resolved_base_dir not in resolved_target.parents:
        raise ValueError(f"Unsafe archive entry: {member_name}")
    return resolved_target


def extract_archive() -> None:
    validate_archive_path(ARCHIVE_PATH)
    DESTINATION_DIR.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(ARCHIVE_PATH) as archive:
        for member in archive.infolist():
            target_path = safe_destination_path(DESTINATION_DIR, member.filename)
            if member.is_dir():
                target_path.mkdir(parents=True, exist_ok=True)
                continue

            target_path.parent.mkdir(parents=True, exist_ok=True)
            with archive.open(member, "r") as source, target_path.open("wb") as target:
                shutil.copyfileobj(source, target)


if __name__ == "__main__":
    try:
        extract_archive()
    except (OSError, ValueError, zipfile.BadZipFile) as exc:
        print(f"Extraction failed: {exc}")