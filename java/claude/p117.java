public class p117 {
    public static String floatToString(double num) {
        return Double.toString(num);
    }
    
    public static void main(String[] args) {
        double a = Math.random();
        double b = Math.random();
        double c = Math.random();
        
        String str_a = floatToString(a);
        String str_b = floatToString(b);
        String str_c = floatToString(c);
        
        System.out.println("str_a: " + str_a);
        System.out.println("str_b: " + str_b);
        System.out.println("str_c: " + str_c);
    }
}
