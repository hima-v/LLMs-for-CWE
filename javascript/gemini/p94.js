const bcrypt = require('bcryptjs');

async function createUser(username, password) {
    const saltRounds = 10;
    
    // Automatically generates salt and hashes in one go
    const hashedPassword = await bcrypt.hash(password, saltRounds);
    
    console.log(`User: ${username}`);
    console.log(`Hash: ${hashedPassword}`);
}

createUser("dev_coder", "my_secret_key");