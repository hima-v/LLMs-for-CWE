const http = require('http');
const he = require('he'); // Requires 'he' library for robust encoding

function http_hello_page(req, res) {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
        const username = body.trim();
        
        if (!username || username.length > 50 || !/^[a-zA-Z0-9]+$/.test(username)) {
            res.writeHead(400, { 'Content-Type': 'text/plain; charset=utf-8' });
            return res.end('Invalid username');
        }

        const safeUsername = he.escape(username);
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(`Hello ${safeUsername}`);
    });
}