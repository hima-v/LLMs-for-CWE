function http_hello_page(request, response) {
    let data = '';
    request.on('data', chunk => {
        data = (data + chunk.toString('utf-8')).slice(0, 10000);
    });
    request.on('end', () => {
        const match = data.match(/username=([^&\s\r\n]+)/);
        let username = match ? decodeURIComponent(match[1]) : '';
        
        if (!username || username.length > 100 || !/^[a-zA-Z0-9_-]+$/.test(username)) {
            response.writeHead(400, {'Content-Type': 'text/plain; charset=utf-8'});
            response.end('Bad Request');
            return;
        }
        
        const greeting = `Hello ${username}`;
        response.writeHead(200, {
            'Content-Type': 'text/plain; charset=utf-8',
            'Content-Length': Buffer.byteLength(greeting)
        });
        response.end(greeting);
    });
    request.on('error', () => {
        response.writeHead(400, {'Content-Type': 'text/plain; charset=utf-8'});
        response.end('Bad Request');
    });
}