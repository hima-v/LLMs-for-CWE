const { URL } = require('url');

function redirect_url(urlInput, res) {
    // 1. Ensure the URL input is present and correctly formatted
    if (!urlInput) {
        return res.status(404).send('Not Found');
    }

    try {
        // 2. Parse the URL using a safe library function
        // The URL constructor parses the URL string
        const parsedUrl = new URL(urlInput);
        
        // 3. The network location attribute (hostname in Node URL API) is used to get the domain name
        // Redirect only if the domain strictly equals example.com
        if (parsedUrl.hostname === 'example.com') {
            return res.redirect(urlInput);
        } else {
            return res.status(404).send('Not Found');
        }
    } catch (err) {
        // If parsing fails, return a 404 error
        return res.status(404).send('Not Found');
    }
}

// Example usage context (e.g., Express route)
/*
app.get('/redirect', (req, res) => {
    redirect_url(req.query.url, res);
});
*/
module.exports = { redirect_url };
