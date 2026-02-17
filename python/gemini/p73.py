# Python abstraction of the concept
class Person:
    def __init__(self):
        self.id = 1


def create_person():
    # Allocates memory for object
    p = Person()
    # Access via dot operator (Python's equivalent)
    p.id = 1
    return p
