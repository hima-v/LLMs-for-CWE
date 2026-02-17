class Person:
    def __init__(self):
        self.status = 0


def allocate():
    p = Person()
    p.status = 0
    return p
