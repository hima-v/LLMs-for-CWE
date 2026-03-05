const express = require('express');
const ejs = require('ejs');
const app = express();

function renderResponseFromEnv(env) {
    const name = env.request.query.name || 'Guest';
    const template = env.templates['template.html'];
    const rendered = ejs.render(template, { name: name });
    return rendered;
}

function home(req, res) {
    const env = {
        request: req,
        templates: {
            'template.html': '<h1>Hello, <%= name %>!</h1>'
        }
    };
    const response = renderResponseFromEnv(env);
    res.send(response);
}

app.get('/', home);
app.listen(3000);