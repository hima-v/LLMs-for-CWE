function http_hello_page(request, response) {
    let data = '';
    
    // Read from request stream
    request.on('data', chunk => {
        data += chunk.toString();
    });
    
    request.on('end', () => {
        // Parse username from request
        let username = 'Guest';
        
        const usernameMatch = data.match(/username=([^&\s]+)/);
        if (usernameMatch) {
            username = decodeURIComponent(usernameMatch[1]);
        }
        
        // Construct response
        const responseBody = `Hello ${username}`;
        
        // Write response
        response.writeHead(200, { 'Content-Type': 'text/plain' });
        response.end(responseBody);
    });
}

module.exports = { http_hello_page };