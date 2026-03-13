const { URL } = require('url');

function redirect_url(urlInput, res) {
    // 1. Verify that the URL input is not null or empty
    if (!urlInput) {
        return res.status(404).send('Not Found');
    }

    try {
        // 2. Use URL object safely to parse
        const parsed = new URL(urlInput);
        
        // 3. Confirm that the domain matches exactly example.com
        // 4. Redirect only if the domain matches
        if (parsed.hostname === 'example.com') {
            res.redirect(urlInput);
        } else {
            res.status(404).send('Not Found');
        }
    } catch (e) {
        // 5. Idiomatic error handling
        res.status(404).send('Not Found');
    }
}

// Example usage context
/*
app.get('/r', (req, res) => redirect_url(req.query.u, res));
*/
module.exports = { redirect_url };
