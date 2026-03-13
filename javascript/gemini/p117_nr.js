const crypto = require('crypto');

function floatToStr(val) {
    // Converts to string; finite check included for safety
    if (!Number.isFinite(val)) return "0.0";
    return val.toFixed(10);
}

function getSecureFloat() {
    // Generate 4 random bytes and map to [0, 1)
    const uint32 = crypto.randomBytes(4).readUint32BE(0);
    return uint32 / (0xFFFFFFFF + 1);
}

const str_a = floatToStr(getSecureFloat());
const str_b = floatToStr(getSecureFloat());
const str_c = floatToStr(getSecureFloat());