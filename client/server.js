var path = require('path');
var fs = require('fs');
var express = require('express');
var _ = require('lodash');

var app = express();


app.use(express.static(path.normalize(`${__dirname}/dist`)));
var bodyParser = require('body-parser')
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
    extended: true
}));


app.get('*', function (request, response){
    response.sendFile(path.normalize(`${__dirname}/dist/eyemed.html`));
});

var server = app.listen(3000, () => {
    var host = server.address().address;
    var port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);
});
