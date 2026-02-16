import java.util.Scanner;

public class p4 {
    public static void main(String[] args) {
        String[] vehicles = {"Car", "Bike", "Truck", "Boat"};
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter vehicle index: ");
        int index = Integer.parseInt(scanner.nextLine());
        System.out.println(vehicles[index]);
    }
}