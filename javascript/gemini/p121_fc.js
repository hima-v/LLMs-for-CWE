function safeCopyAndPrint(input) {
    if (typeof input !== 'string') return;

    // Strings are primitive and immutable in JS
    // The engine handles allocation and garbage collection
    const buffer = String(input);
    console.log(`Output: ${buffer}`);
}