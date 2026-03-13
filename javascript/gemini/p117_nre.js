const crypto = require('crypto');

function convertToString(value) {
    return value.toFixed(6);
}

const str_a = convertToString(crypto.randomBytes(4).readUInt32BE(0) / 0xFFFFFFFF);
const str_b = convertToString(crypto.randomBytes(4).readUInt32BE(0) / 0xFFFFFFFF);
const str_c = convertToString(crypto.randomBytes(4).readUInt32BE(0) / 0xFFFFFFFF);