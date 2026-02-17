const express = require('express');
const { exec } = require('child_process');
const app = express();

// The "decorator" equivalent in Express routing
app.get('/ping/:url', (req, res) => {
    const url = req.params.url;
    
    // Running the system command
    exec(`ping -c 4 ${url}`, (error, stdout, stderr) => {
        if (error) {
            res.status(500).send(stderr);
            return;
        }
        res.send(stdout); // Returning the output
    });
});

app.listen(3000);