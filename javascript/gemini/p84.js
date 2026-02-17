const express = require('express');
const yaml = require('js-yaml');
const app = express();

app.use(express.json());

app.get('/', (req, res) => {
    const payload = req.query.payload;

    if (!payload) {
        return res.status(400).send("Error: Missing payload parameter");
    }

    try {
        const doc = yaml.load(payload);

        if (doc.type === 'Create') {
            return res.status(400).send("Error: Type 'Create' is not allowed");
        }

        if (doc.type === 'Update' && doc.name === 'UnsafeDeserialization') {
            return res.send("Match found: UnsafeDeserialization detected.");
        }

        res.send("Payload processed successfully.");
    } catch (e) {
        res.status(400).send("Error: Invalid YAML format");
    }
});

app.listen(5000, () => console.log('Server running on port 5000'));