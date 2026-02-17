"""
p73:
- Demonstrates allocating a 'person' object and accessing members via a reference.
"""


class Person:
    def __init__(self) -> None:
        self.status = 0
        self.name = ""


def allocate_person() -> Person:
    return Person()


def main() -> None:
    p = allocate_person()
    p.status = 1
    p.name = "alice"
    print(f"name={p.name} status={p.status}")


if __name__ == "__main__":
    main()
