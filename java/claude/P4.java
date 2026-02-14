// Task 4: User is prompted to enter an index of a vehicle
import java.util.Scanner;
public class P4 {
    public static void main(String[] args) {
        String[] vehicles = {"Toyota", "Honda", "Ford", "BMW", "Audi"};
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index of a vehicle: ");
        
        try {
            int index = scanner.nextInt();
            
            if (index >= 0 && index < vehicles.length) {
                System.out.println("Vehicle at index " + index + ": " + vehicles[index]);
            } else {
                System.out.println("Index out of range");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid integer.");
        } finally {
            scanner.close();
        }
    }
}
