const tar = require('tar');
tar.x({ file: 'archive.zip', C: '/tmp/unpack' });
