const { URL } = require('url');

function redirect_url(res, urlInput) {
    // 1. Validate input
    if (!urlInput) {
        return res.status(404).send('Not Found');
    }

    try {
        // 2. Parse URL safely
        const parsed = new URL(urlInput);
        
        // 3. Check exact match
        if (parsed.hostname === 'example.com') {
            res.redirect(urlInput);
        } else {
            // 4. Reject & 5. Return 404
            res.status(404).send('Not Found');
        }
    } catch (e) {
        res.status(404).send('Not Found');
    }
}

module.exports = { redirect_url };
