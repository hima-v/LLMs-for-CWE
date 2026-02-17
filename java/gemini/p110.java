public class UserCheck {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java UserCheck <username>");
            System.exit(1);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("id", args[0]);
            pb.inheritIO(); // Directs output to the console
            Process process = pb.start();
            
            int exitStatus = process.waitFor();
            System.out.println("Exit status: " + exitStatus);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}