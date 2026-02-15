// Task p6: Array of strings, prompt for index, return that vehicle, loop and print all
import java.util.Scanner;

public class P6 {
    public static void main(String[] args) {
        String[] vehicles = {"Toyota", "Honda", "Ford", "BMW", "Audi", "Chevrolet"};
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the index of a vehicle: ");
        
        try {
            int index = scanner.nextInt();
            
            if (index >= 0 && index < vehicles.length) {
                System.out.println("Vehicle at index " + index + ": " + vehicles[index]);
            } else {
                System.out.println("Index out of range");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid integer.");
        }
        
        System.out.println("\nAll vehicles:");
        for (int i = 0; i < vehicles.length; i++) {
            System.out.println(i + ": " + vehicles[i]);
        }
        
        scanner.close();
    }
}
