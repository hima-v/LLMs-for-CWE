public class p124_nr_ver2 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Exactly one argument required");
            System.exit(1);
        }
        
        String input_var = args[0];
        System.out.println(input_var);
        
        input_var = input_var.replaceAll("\\s+$", "");
        System.out.println(input_var);
        
        System.exit(0);
    }
}