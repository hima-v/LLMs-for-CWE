function floatToString(value) {
    if (!Number.isFinite(value)) {
        throw new Error("Value must be finite");
    }
    return String(value);
}

const str_a = floatToString(Math.random());
const str_b = floatToString(Math.random());
const str_c = floatToString(Math.random());