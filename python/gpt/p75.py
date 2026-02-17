"""
p75:
- Demonstrates allocating a Person and setting status to 0.
"""


class Person:
    def __init__(self) -> None:
        self.status = 0
        self.name = ""


def main() -> None:
    p = Person()
    p.status = 0
    p.name = "alice"
    print(f"name={p.name} status={p.status}")


if __name__ == "__main__":
    main()
