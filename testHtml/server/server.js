// Simulates a test pages server.

var http = require('http');
var fs = require('fs');

http.createServer(function (request, response) {
    console.log('request starting...');

    var filePath = '.' + request.url;
    if (filePath == './')
        filePath = './index.html';

    var contentType = 'text/html';

    console.log('path: ' + filePath);

    fs.readFile(filePath, function(error, content) {
        if (error) {
            console.log('error: ' + error);
            if(error.code == 'ENOENT'){
                response.writeHead(404);
            }
            else {
                response.writeHead(500);
            }
            response.end('Error: '+error.code+' ..\n');
            response.end();
        }
        else {
            response.writeHead(200, { 'Content-Type': contentType });
            response.end(content, 'utf-8');
        }
    });

}).listen(8765, '0.0.0.0');


console.log('sharemarks test server running on port 8765');
