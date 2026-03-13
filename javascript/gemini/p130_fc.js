const express = require('express');
const app = express();
// Template is pre-compiled/fixed
const ejs = require('ejs');
const TEMPLATE = "<h1>Hello, <%= name %>!</h1>";

function render_response_from_env(env) {
    let name = env.query.name || "Guest";
    // Validation: alphanumeric, max 20 chars
    if (!/^[a-zA-Z0-9]{1,20}$/.test(name)) {
        name = "Guest";
    }
    // EJS <%= %> performs HTML entity encoding by default
    return ejs.render(TEMPLATE, { name: name });
}

app.get('/home', (req, res) => {
    res.send(render_response_from_env(req));
});