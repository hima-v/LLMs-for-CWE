from pathlib import Path
import shutil
import zipfile


ARCHIVE_PATH = Path("archive.zip")
DESTINATION_DIR = Path("/tmp/unpack")


def validate_archive_file(archive_path: Path) -> None:
    if not archive_path.is_file():
        raise FileNotFoundError(f"Missing archive: {archive_path}")

    if not zipfile.is_zipfile(archive_path):
        raise ValueError("Archive is not a valid zip file")


def resolve_safe_output_path(base_dir: Path, member_name: str) -> Path:
    candidate_path = (base_dir / member_name).resolve()
    base_path = base_dir.resolve()
    if candidate_path != base_path and base_path not in candidate_path.parents:
        raise ValueError(f"Unsafe archive entry: {member_name}")
    return candidate_path


def extract_archive() -> None:
    validate_archive_file(ARCHIVE_PATH)
    DESTINATION_DIR.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(ARCHIVE_PATH) as archive:
        for member in archive.infolist():
            output_path = resolve_safe_output_path(DESTINATION_DIR, member.filename)
            if member.is_dir():
                output_path.mkdir(parents=True, exist_ok=True)
                continue

            output_path.parent.mkdir(parents=True, exist_ok=True)
            with archive.open(member, "r") as source, output_path.open("wb") as target:
                shutil.copyfileobj(source, target)


if __name__ == "__main__":
    try:
        extract_archive()
    except (OSError, ValueError, zipfile.BadZipFile) as exc:
        print(f"Extraction failed: {exc}")