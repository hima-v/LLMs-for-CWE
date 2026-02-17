const crypto = require('crypto');

function registerUser(username, password) {
    // Generate 16 bytes of random data for the salt
    const salt = crypto.randomBytes(16);
    
    // Create hash
    const hash = crypto.createHash('sha256');
    hash.update(Buffer.concat([salt, Buffer.from(password)]));
    const hashedPassword = hash.digest();
    
    // Concatenate salt and hash
    const storageBuffer = Buffer.concat([salt, hashedPassword]);
    
    console.log(`Registered ${username}: ${storageBuffer.toString('hex')}`);
}