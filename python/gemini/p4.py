vehicles = ["Car", "Bike", "Truck", "Boat"]
try:
    index = int(input("Enter vehicle index: "))
    print(vehicles[index])
except (ValueError, IndexError):
    print("Invalid index")
