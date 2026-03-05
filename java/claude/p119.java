public class RandomFloats {
    public static void main(String[] args) {
        // Generate three random floats
        float rand1 = (float) Math.random();
        float rand2 = (float) Math.random();
        float rand3 = (float) Math.random();
        
        // Convert to strings
        String string = Float.toString(rand1);
        String string2 = Float.toString(rand2);
        String string3 = Float.toString(rand3);
        
        System.out.println("string: " + string);
        System.out.println("string2: " + string2);
        System.out.println("string3: " + string3);
    }
}
