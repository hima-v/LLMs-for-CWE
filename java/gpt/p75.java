/*
 * p75:
 * - Allocates a Person and sets status to 0.
 */

public class p75 {
    static class Person {
        int status;
        String name;
    }

    public static void main(String[] args) {
        Person p = new Person();
        p.status = 0;
        p.name = "alice";
        System.out.println("name=" + p.name + " status=" + p.status);
    }
}
