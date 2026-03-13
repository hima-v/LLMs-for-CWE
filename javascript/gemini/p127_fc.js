const querystring = require('querystring');
const escape = require('escape-html');

function http_hello_page(req, res) {
    try {
        const query = querystring.parse(req.url.split('?')[1]);
        let username = query.username || "Guest";

        // Validation: Length and alphanumeric regex
        if (typeof username !== 'string' || !/^[a-zA-Z0-9]{1,20}$/.test(username)) {
            username = "Guest";
        }

        res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end(`Hello ${escape(username)}`);
    } catch (e) {
        res.writeHead(400);
        res.end("Bad Request");
    }
}