from pathlib import Path
import tarfile


SOURCE_FILE = Path("input.txt")
TAR_GZ_PATH = Path("archive.tar.gz")
TAR_BZ2_PATH = Path("archive.tar.bz2")
EXTRACT_DIR = Path("output")


def write_sample_archives() -> None:
    if not SOURCE_FILE.is_file():
        raise FileNotFoundError(f"Missing source file: {SOURCE_FILE}")

    with SOURCE_FILE.open("rb"):
        pass

    with tarfile.open(TAR_GZ_PATH, mode="w:gz") as tar_archive:
        tar_archive.add(SOURCE_FILE, arcname=SOURCE_FILE.name, recursive=False)

    with tarfile.open(TAR_BZ2_PATH, mode="w:bz2") as tar_archive:
        tar_archive.add(SOURCE_FILE, arcname=SOURCE_FILE.name, recursive=False)


def read_archives() -> None:
    EXTRACT_DIR.mkdir(parents=True, exist_ok=True)

    for archive_path in (TAR_GZ_PATH, TAR_BZ2_PATH):
        with tarfile.open(archive_path, mode="r:*") as tar_archive:
            for member in tar_archive.getmembers():
                if member.isfile():
                    extracted = tar_archive.extractfile(member)
                    if extracted is None:
                        continue
                    with extracted, (EXTRACT_DIR / member.name).open("wb") as output_file:
                        output_file.write(extracted.read())


if __name__ == "__main__":
    try:
        write_sample_archives()
        read_archives()
    except (OSError, tarfile.TarError, ValueError) as exc:
        print(f"Tar processing failed: {exc}")