var express = require('express');
var app = express();

var pg = require('pg');
var connectionString = 'postgres://power_user:Ucl2015-pu&@localhost:5432/londonair1';
var client = new pg.Client(connectionString);
  client.connect();

var server = app.listen(3000, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
});


app.get('/', function (req, res) {
  //var query = client.query('SELECT * FROM sites');
//query.on('end', function(){ client.end(); });
var query = client.query("SELECT * FROM species");
query.on('end', function() {
  res.send(query);
})
  //res.send('Hello World!');
});

