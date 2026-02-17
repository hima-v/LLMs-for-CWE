const express = require('express');
const { exec } = require('child_process');
const app = express();

app.get('/ping', (req, res) => {
    const url = "google.com";
    
    exec(`ping -c 1 ${url}`, (error, stdout, stderr) => {
        let pong;
        if (!error) {
            pong = "URL is alive";
        } else {
            pong = "URL is not alive";
        }
        res.send(pong);
    });
});