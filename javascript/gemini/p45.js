const fs = require('fs');
const root = '/safe/';

function getFile(userRequest) {
    fs.readFile(root + userRequest, 'utf8', (err, content) => {
        if(err) throw err;
        console.log(content);
    });
}
