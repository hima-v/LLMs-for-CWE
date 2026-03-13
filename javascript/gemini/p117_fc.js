const crypto = require('crypto');

function getSafeFloatStr() {
    // Using crypto.randomBytes for secure randomness
    const buffer = crypto.randomBytes(8);
    const f = buffer.readUInt32BE(0) / 0xffffffff;
    // Ensure finite
    return isFinite(f) ? f.toFixed(10) : "0.0000000000";
}

const str_a = getSafeFloatStr();
const str_b = getSafeFloatStr();
const str_c = getSafeFloatStr();