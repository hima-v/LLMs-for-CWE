const crypto = require('crypto');

function registerUser(username, password) {
    const salt = crypto.randomBytes(16).toString('hex');
    
    // Scrypt is a password-based key derivation function
    crypto.scrypt(password, salt, 64, (err, derivedKey) => {
        if (err) throw err;
        
        const user = {
            username: username,
            salt: salt,
            hash: derivedKey.toString('hex')
        };
        console.log("Registration successful for:", user.username);
    });
}

registerUser("WebDev_99", "MySecretPassword");