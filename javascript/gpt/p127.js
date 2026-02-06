function http_hello_page(request, response) {
    let data = "";

    request.on("data", chunk => {
        data += chunk;
    });

    request.on("end", () => {
        const username = data.trim();
        response.write(`Hello ${username}`);
        response.end();
    });
}
