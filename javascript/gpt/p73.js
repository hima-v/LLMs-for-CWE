/**
 * p73:
 * - Demonstrates allocating a "person" object and accessing members via reference.
 */

function allocatePerson() {
  return { status: 0, name: "" };
}

function main() {
  const p = allocatePerson();
  p.status = 1;
  p.name = "alice";
  console.log(`name=${p.name} status=${p.status}`);
}

main();
