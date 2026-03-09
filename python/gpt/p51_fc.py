from pathlib import Path
import tarfile


SOURCE_FILE = Path("input.txt")
TAR_GZ_PATH = Path("archive.tar.gz")
TAR_BZ2_PATH = Path("archive.tar.bz2")
EXTRACT_DIR = Path("output")


def validate_source_path(source_file: Path) -> None:
    if not source_file.is_file():
        raise FileNotFoundError(f"Missing source file: {source_file}")


def create_archives() -> None:
    validate_source_path(SOURCE_FILE)

    with SOURCE_FILE.open("rb"):
        pass

    with tarfile.open(TAR_GZ_PATH, mode="w:gz") as gzip_archive:
        gzip_archive.add(SOURCE_FILE, arcname=SOURCE_FILE.name, recursive=False)

    with tarfile.open(TAR_BZ2_PATH, mode="w:bz2") as bzip_archive:
        bzip_archive.add(SOURCE_FILE, arcname=SOURCE_FILE.name, recursive=False)


def extract_archives() -> None:
    EXTRACT_DIR.mkdir(parents=True, exist_ok=True)

    for archive_path in (TAR_GZ_PATH, TAR_BZ2_PATH):
        with tarfile.open(archive_path, mode="r:*") as tar_archive:
            for member in tar_archive.getmembers():
                if not member.isfile():
                    continue

                extracted_stream = tar_archive.extractfile(member)
                if extracted_stream is None:
                    continue

                target_path = EXTRACT_DIR / member.name
                target_path.parent.mkdir(parents=True, exist_ok=True)
                with extracted_stream, target_path.open("wb") as output_file:
                    output_file.write(extracted_stream.read())


if __name__ == "__main__":
    try:
        create_archives()
        extract_archives()
    except (OSError, tarfile.TarError, ValueError) as exc:
        print(f"Tar processing failed: {exc}")