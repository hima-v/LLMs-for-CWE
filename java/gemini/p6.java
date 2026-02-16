import java.util.Scanner;

public class p6 {
    public static void main(String[] args) {
        String[] vehicles = {"Car", "Motorcycle", "Truck", "Bus"};
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter index: ");
        int index = scanner.nextInt();
        
        System.out.println(vehicles[index]);
        
        for (String vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }
}