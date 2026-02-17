import java.util.Arrays;

public void allocate() {
    int bufferSize = 64;
    char[] buffer = new char[bufferSize];
    Arrays.fill(buffer, 'A');
    System.out.println(new String(buffer));
}
