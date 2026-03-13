// JavaScript (uses CSPRNG via crypto.getRandomValues)
function randFloat01() {
  const u32 = new Uint32Array(2);
  crypto.getRandomValues(u32);
  const hi = BigInt(u32[0]);
  const lo = BigInt(u32[1]);
  const x64 = (hi << 32n) | lo;
  const x53 = x64 >> 11n; // keep top 53 bits
  const f = Number(x53) / 9007199254740992; // 2^53
  if (!Number.isFinite(f)) throw new Error("non-finite float");
  return f;
}

function floatToStr(f) {
  if (!Number.isFinite(f)) throw new Error("non-finite float");
  return f.toPrecision(17); // explicit formatting (no user-controlled format string)
}

const a = randFloat01();
const b = randFloat01();
const c = randFloat01();

const str_a = floatToStr(a);
const str_b = floatToStr(b);
const str_c = floatToStr(c);