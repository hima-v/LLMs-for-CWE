// JavaScript (Express + EJS)
const express = require('express');
const app = express();

const ALLOWED_TEMPLATES = {
    default: (name) => `<h1>Hello, <%= name %>!</h1>`
};

function renderResponseFromEnv(env) {
    let name = (new URL(request.url, `http://${request.headers.host}`)).searchParams.get('name') || 'Guest';
    
    // Validate name parameter
    if (!name || name.length > 100) {
        name = 'Guest';
    }
    if (!/^[a-zA-Z0-9\s\-_]+$/.test(name)) {
        name = 'Guest';
    }
    
    // Get template from allowlisted templates
    const template = ALLOWED_TEMPLATES.default;
    
    // Render with proper escaping
    const response = template(name).replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return response;
}

function home(request, response) {
    const env = null;
    const result = renderResponseFromEnv(env);
    response.send(result);
}

app.get('/', home);