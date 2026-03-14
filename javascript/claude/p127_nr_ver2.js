// JAVASCRIPT (Node.js)
async function http_hello_page(request, response) {
    try {
        let requestData = '';
        
        for await (const chunk of request) {
            requestData += chunk.toString('utf-8');
            if (requestData.length > 4096) break;
        }
        
        const usernameMatch = requestData.match(/username=([^&\n\r]+)/);
        if (!usernameMatch) {
            response.write('HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid request');
            return;
        }
        
        let username = decodeURIComponent(usernameMatch[1]);
        
        if (username.length === 0 || username.length > 256) {
            response.write('HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nUsername length invalid');
            return;
        }
        
        if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
            response.write('HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nUsername contains invalid characters');
            return;
        }
        
        const message = `Hello ${username}`;
        const messageBuffer = Buffer.from(message, 'utf-8');
        response.write('HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: ' + messageBuffer.length + '\r\n\r\n');
        response.write(messageBuffer);
    } catch (err) {
        response.write('HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nServer error');
    }
}