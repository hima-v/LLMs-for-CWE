const express = require('express');
const app = express();

// Set up EJS (auto-escapes by default with <%= %>)
app.set('view engine', 'ejs');

const ALLOWED_TEMPLATES = { 'home': 'home.ejs' };

function render_response_from_env(envName, userInput) {
    const template = ALLOWED_TEMPLATES[envName];
    if (!template) throw new Error("Invalid template");

    // Validate input
    const sanitizedName = typeof userInput === 'string' && userInput.length < 20 
        ? userInput.replace(/[^a-zA-Z0-9]/g, '') 
        : 'Guest';

    return { template, data: { name: sanitizedName } };
}

app.get('/', (req, res) => {
    const { template, data } = render_response_from_env('home', req.query.name);
    res.render(template, data);
});