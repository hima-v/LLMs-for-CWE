const fs = require('fs');
const mysql = require('mysql');

const img = fs.readFileSync('image.jpg');
const connection = mysql.createConnection({/*...*/});

connection.query('INSERT INTO images SET ?', { data: img }, (err, res) => {
    if (err) throw err;
    console.log('Image stored');
});
