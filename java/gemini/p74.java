class Person {
    int status;
}

public Person allocate() {
    Person p = new Person();
    p.status = 0;
    return p;
}
