public class p124_nr_ver3 {
    private static String rtrim(String s) {
        int i = s.length();
        while (i > 0 && Character.isWhitespace(s.charAt(i - 1))) {
            i--;
        }
        return s.substring(0, i);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: expected exactly one argument.");
            System.exit(1);
        }

        String input = args[0];
        System.out.println(input);

        String trimmed = rtrim(input);
        System.out.println(trimmed);

        System.exit(0);
    }
}