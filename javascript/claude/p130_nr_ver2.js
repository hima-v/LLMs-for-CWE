// JavaScript (Node.js + Express + EJS)
const express = require('express');
const app = express();

const ALLOWED_TEMPLATES = { greeting: 'greeting', welcome: 'welcome' };
const NAME_PATTERN = /^[a-zA-Z0-9\s\-_]{1,50}$/;

function renderResponseFromEnv(env) {
    const templateName = this.query.template || 'greeting';
    if (!(templateName in ALLOWED_TEMPLATES)) {
        return { error: 'Invalid template' };
    }
    
    let name = this.query.name || 'Guest';
    if (!NAME_PATTERN.test(name)) {
        name = 'Guest';
    }
    
    return { template: ALLOWED_TEMPLATES[templateName], name: name };
}

app.get('/', function(req, res) {
    const result = renderResponseFromEnv.call(req, null);
    res.render(result.template, { name: result.name });
});

app.set('view engine', 'ejs');
app.listen(3000);