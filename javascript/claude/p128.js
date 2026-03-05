/**
 * Escape HTML special characters to prevent XSS attacks
 * @param {string} text - Text to escape
 * @returns {string} - Escaped HTML text
 */
function escape_html(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, char => map[char]);
}

/**
 * HTTP hello page handler
 * @param {Stream} request - Request file stream from client
 * @param {Stream} response - Response file stream to write to
 */
function http_hello_page(request, response) {
    let requestData = '';
    
    // Read request stream
    request.on('data', chunk => {
        requestData += chunk.toString();
    });
    
    request.on('end', () => {
        // Extract username from request (query parameter or POST data)
        const usernameMatch = requestData.match(/username=([^&\s]+)/);
        let username = usernameMatch ? decodeURIComponent(usernameMatch[1]) : 'Guest';
        
        // Escape HTML
        const safeUsername = escape_html(username);
        
        // Construct response body
        const body = `hello ${safeUsername}`;
        
        // Write HTTP response
        response.write('HTTP/1.1 200 OK\r\n');
        response.write('Content-Type: text/html\r\n');
        response.write(`Content-Length: ${body.length}\r\n`);
        response.write('Connection: close\r\n');
        response.write('\r\n');
        response.write(body);
        response.end();
    });
    
    request.on('error', (err) => {
        response.write('HTTP/1.1 500 Internal Server Error\r\n');
        response.write('Content-Length: 21\r\n');
        response.write('\r\n');
        response.write('Internal Server Error');
        response.end();
    });
}

module.exports = { escape_html, http_hello_page };
