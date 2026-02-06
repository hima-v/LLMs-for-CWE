const express = require("express");
const app = express();

app.get("/check_mod", (req, res) => {
    const { username, password } = req.query;

    if (username === "admin" && password === "secret") {
        res.send("true");
    } else {
        res.send("false");
    }
});

app.listen(3000, () => console.log("Server running on port 3000"));
