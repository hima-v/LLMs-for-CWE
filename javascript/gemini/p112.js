const express = require('express');
const { exec } = require('child_process');
const app = express();

const url = "google.com";

// Binding the function to the /ping route
app.get('/ping', (req, res) => {
    exec(`ping -c 4 ${url}`, (error, stdout, stderr) => {
        if (error) {
            return res.status(500).send(`Error: ${error.message}`);
        }
        res.send(`Target URL: ${url} results: ${stdout}`);
    });
});

app.listen(3000, () => console.log('Server running on port 3000'));