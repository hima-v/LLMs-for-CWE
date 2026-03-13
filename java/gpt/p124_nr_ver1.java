// Java
public class p124_nr_ver1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: expected exactly one argument.");
            System.exit(2);
        }

        String input = args[0];
        System.out.println(input);

        String trimmed = input.replaceAll("\\s+$", "");
        System.out.println(trimmed);

        System.exit(0);
    }
}