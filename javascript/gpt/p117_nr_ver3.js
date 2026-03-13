import { randomBytes } from "crypto";

function randomFiniteFloat() {
  // Security-sensitive: cryptographically secure RNG (Node.js crypto)
  const buf = randomBytes(8);
  const bits = (BigInt(buf.readUInt32BE(0)) << 32n) | BigInt(buf.readUInt32BE(4));
  const mantissa = Number(bits & ((1n << 53n) - 1n));
  const x = mantissa / 2 ** 53; // [0, 1)
  if (!Number.isFinite(x)) throw new Error("Non-finite float generated");
  return x;
}

const a = randomFiniteFloat();
const b = randomFiniteFloat();
const c = randomFiniteFloat();

const str_a = a.toPrecision(17);
const str_b = b.toPrecision(17);
const str_c = c.toPrecision(17);