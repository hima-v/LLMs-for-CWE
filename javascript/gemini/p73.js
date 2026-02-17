class Person {
    constructor() {
        this.id = 1;
    }
}

function createPerson() {
    const p = new Person(); // Allocation
    p.id = 1;
    return p;
}
