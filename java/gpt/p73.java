/*
 * p73:
 * - Demonstrates allocating a Person object and accessing fields through a reference
 */

public class p73 {
    static class Person {
        int status;
        String name;
    }

    static Person allocatePerson() {
        return new Person();
    }

    public static void main(String[] args) {
        Person p = allocatePerson();
        p.status = 1;
        p.name = "alice";
        System.out.println("name=" + p.name + " status=" + p.status);
    }
}
