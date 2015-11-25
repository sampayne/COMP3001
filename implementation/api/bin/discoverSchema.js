var path = require('path');

var app = require(path.resolve(__dirname, '../server/server'));
var ds = app.datasources.airmazingdb;
ds.discoverSchema('user', {schema: 'public'}, function(err, schema) {
  if (err) throw err;
  console.log(err);
  var json = JSON.stringify(schema, null, '  ');
  console.log(json);

  ds.disconnect();
});