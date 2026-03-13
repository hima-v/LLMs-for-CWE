const nunjucks = require('nunjucks');
const env = new nunjucks.Environment();

const ALLOWED_TEMPLATES = new Set(['home.html']);

function validateName(name) {
    // SECURE: Strict regex validation
    return /^[A-Za-z0-9]{1,20}$/.test(name) ? name : "Guest";
}

function renderResponseFromEnv(env, templateName, req) {
    if (!ALLOWED_TEMPLATES.has(templateName)) {
        throw new Error("Invalid template");
    }
    
    const safeName = validateName(req.query.name || "");
    // SECURE: Nunjucks auto-escapes HTML by default
    return env.render(templateName, { name: safeName });
}

function home(req, res) {
    const response = renderResponseFromEnv(env, 'home.html', req);
    res.send(response);
}