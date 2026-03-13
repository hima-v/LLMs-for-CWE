public class p121_fc {
    public static void safeCopyAndPrint(String input) {
        if (input == null) return;

        // Java Strings are immutable objects
        // No manual buffer management required (avoids CWE-120/416/415)
        String buffer = new String(input);
        System.out.println("Output: " + buffer);
    }
}