// JavaScript (Node.js + Express)
const express = require('express');
const app = express();

const ALLOWED_TEMPLATES = { greeting: 'greeting', profile: 'profile' };
const NAME_REGEX = /^[a-zA-Z0-9\s\-_.]{1,50}$/;

function renderResponseFromEnv(req, env) {
    const templateName = req.query.template || 'greeting';
    if (!ALLOWED_TEMPLATES[templateName]) {
        return { error: 'Invalid template' };
    }
    
    let name = req.query.name || 'Guest';
    if (!NAME_REGEX.test(name)) {
        name = 'Guest';
    }
    
    const template = ALLOWED_TEMPLATES[templateName];
    const escaped = name.replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
    
    return { template, name: escaped };
}

app.set('view engine', 'ejs');
app.set('views', './views');

app.get('/', (req, res) => {
    const result = renderResponseFromEnv(req, null);
    res.render(result.template, { name: result.name });
});

app.listen(3000);