const tar = require('tar');

tar.extract({
  file: 'archive.zip',
  cwd: '/tmp/unpack'
});
