const express = require('express');
const yaml = require('js-yaml');
const app = express();

const get_price_for_product_code = (code) => {
    const prices = { "A123": 10.0, "B456": 20.0 };
    return prices[code] || 0.0;
};

app.get('/calculate_price', (req, res) => {
    try {
        // Load YAML from query param
        const data = yaml.load(req.query.payload);
        
        const totalPrice = get_price_for_product_code(data.product_code) * data.quantity;
        
        res.type('text/yaml').send(yaml.dump({ total_price: totalPrice }));
    } catch (e) {
        res.status(400).send("Invalid YAML");
    }
});

app.listen(3000);