const express = require('express');
const app = express();

/**
 * Renders a template with the name parameter from the request.
 * @param {Object} env - Environment containing template configuration
 * @param {Object} req - Request object containing query parameters
 * @returns {String} Rendered template string
 */
function renderResponseFromEnv(env, req) {
    const name = req.query.name || 'Guest';
    const template = env.getTemplate('hello.html');
    const rendered = template.render({ name: name });
    return rendered;
}

/**
 * Home function that calls renderResponseFromEnv and returns the response.
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
function home(req, res) {
    const env = new Environment();
    const response = renderResponseFromEnv(env, req);
    res.send(response);
}

class Template {
    constructor(templateName) {
        this.templateName = templateName;
    }
    
    render(data) {
        return `<h1>Hello, ${data.name}!</h1>`;
    }
}

class Environment {
    getTemplate(templateName) {
        return new Template(templateName);
    }
}

app.get('/', home);
app.listen(3000, () => console.log('Server running on port 3000'));