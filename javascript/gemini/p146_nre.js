app.post('/buy', async (req, res) => {
    const username = req.session.username;
    if (!username) return res.status(401).send();

    const { stock_name, stock_quantity } = req.body;
    const qty = parseInt(stock_quantity);

    if (!/^[A-Z]{1,5}$/.test(stock_name) || isNaN(qty) || qty < 1 || qty > 100000) {
        return res.status(400).send();
    }

    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        await client.query(
            'INSERT INTO orders (username, stock_name, quantity) VALUES ($1, $2, $3)',
            [username, stock_name, qty]
        );
        await client.query('COMMIT');
        res.redirect('/profile');
    } catch (e) {
        await client.query('ROLLBACK');
        res.status(500).send();
    } finally {
        client.release();
    }
});