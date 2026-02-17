class Person {
    int id;
}

public Person createPerson() {
    // 'new' allocates memory
    Person p = new Person();
    p.id = 1;
    return p;
}
