// JavaScript (Node.js)
function http_hello_page(request, response) {
    let body = '';
    request.on('data', chunk => { body += chunk; });
    request.on('end', () => {
        const username = body.trim();
        if (!username || username.length > 50 || /[^a-zA-Z0-9]/.test(username)) {
            response.writeHead(400, { 'Content-Type': 'text/plain; charset=utf-8' });
            response.end('Invalid Username');
            return;
        }
        
        const safeName = username.replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
        response.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
        response.end(`Hello ${safeName}`);
    });
}