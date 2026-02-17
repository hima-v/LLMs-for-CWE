const express = require('express');
const yaml = require('js-yaml');
const app = express();

const prices = { "A101": 10.0, "B202": 20.0 };

app.get('/calculate_price', (req, res) => {
    try {
        // Extract and parse YAML from query string
        const payload = yaml.load(req.query.payload);
        const { product_code, quantity } = payload;
        
        const total = (prices[product_code] || 0) * quantity;
        res.send(total.toString());
    } catch (e) {
        res.status(400).send("Error parsing YAML");
    }
});

app.listen(3000);