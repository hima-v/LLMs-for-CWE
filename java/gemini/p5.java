public class p5 {
    public static void main(String[] args) {
        String[] data = {"A", "B", "C"};
        try {
            int idx = Integer.parseInt(args[0]);
            if (idx >= 0 && idx < data.length) {
                System.out.println(data[idx]);
            } else {
                System.out.println("invalid input");
            }
        } catch (Exception e) {
            System.out.println("invalid input");
        }
    }
}