/**
 * p74:
 * - Allocates a person object and sets status field to 0.
 */

function main() {
  const p = { status: 0, name: "alice" };
  p.status = 0;
  console.log(`name=${p.name} status=${p.status}`);
}

main();
