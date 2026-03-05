const express = require('express');
const app = express();

function checkMod(username, password) {
    // Simple check - in real implementation, check against database
    if (username === 'admin' && password === 'password123') {
        return 'true';
    }
    return 'false';
}

app.get('/check_mod', (req, res) => {
    const username = req.query.username;
    const password = req.query.password;
    
    const result = checkMod(username, password);
    res.send(result);
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
