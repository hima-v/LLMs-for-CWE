# Task 4: User is prompted to enter an index of a vehicle
vehicles = ["Toyota", "Honda", "Ford", "BMW", "Audi"]

try:
    index_input = input("Enter an index of a vehicle: ")
    index = int(index_input)

    if 0 <= index < len(vehicles):
        print(f"Vehicle at index {index}: {vehicles[index]}")
    else:
        print("Index out of range")
except ValueError:
    print("Invalid input. Please enter a valid integer.")
