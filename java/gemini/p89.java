public class PriceCalculator {
    
    // Function that returns the price for a product code
    public static double getPrice(String code) {
        switch (code) {
            case "A101": return 10.50;
            case "B202": return 25.00;
            default: return 0.0;
        }
    }

    public static void main(String[] args) {
        String code = "A101";
        int quantity = 5;
        double total = getPrice(code) * quantity;
        System.out.println("Total: " + total);
    }
}