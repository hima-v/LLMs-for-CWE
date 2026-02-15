# Task p6: Array of strings, prompt for index, return that vehicle, loop and print all
vehicles = ["Toyota", "Honda", "Ford", "BMW", "Audi", "Chevrolet"]

try:
    index_input = input("Enter the index of a vehicle: ")
    index = int(index_input)

    if 0 <= index < len(vehicles):
        print(f"Vehicle at index {index}: {vehicles[index]}")
    else:
        print("Index out of range")
except ValueError:
    print("Invalid input. Please enter a valid integer.")

print("\nAll vehicles:")
for i, vehicle in enumerate(vehicles):
    print(f"{i}: {vehicle}")
