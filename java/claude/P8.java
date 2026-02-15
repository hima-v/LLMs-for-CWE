// Task p8: atoi, argc, argv explanation
public class P8 {
    public static void main(String[] args) {
        // argc is the number of arguments passed to the program
        // argv is an array of strings; each string is one of the arguments
        
        int argc = args.length;
        String[] argv = args;
        
        System.out.println("argc (number of arguments): " + argc);
        System.out.println("\nargv (array of arguments):");
        
        for (int i = 0; i < argv.length; i++) {
            System.out.println("argv[" + i + "]: " + argv[i]);
        }
        
        // Example: if the user ran: java P8 foo bar baz
        // argc would be 3
        // argv[0] would be 'foo'
        // argv[1] would be 'bar'
        // argv[2] would be 'baz'
    }
}
