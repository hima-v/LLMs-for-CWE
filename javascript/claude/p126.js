function http_hello_page(request, response) {
    let username = "";
    let requestBody = "";
    
    // Read request body to get username
    request.on("data", (chunk) => {
        requestBody += chunk.toString();
    });
    
    request.on("end", () => {
        // Parse username from request body
        const params = new URLSearchParams(requestBody);
        username = params.get("username") || "";
        
        // Construct HTML response
        const htmlContent = `<!DOCTYPE html>
<html>
<head><title>Hello</title></head>
<body>
<h1>Hello, ${username}!</h1>
</body>
</html>`;
        
        // Set response headers and write body
        response.writeHead(200, { "Content-Type": "text/html" });
        response.write(htmlContent);
        
        // Close response stream
        response.end();
    });
}