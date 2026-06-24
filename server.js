// Micro-service d'évaluation de mot de passe (zxcvbn)
const http = require('http');
const zxcvbn = require('zxcvbn');
const server = http.createServer((req, res) => {
    
    if (req.url !== '/zxcvbn') {
        res.writeHead(404);
        res.end();
        return;
    }
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
        try {
            const { password } = JSON.parse(body);
            const result = zxcvbn(password);
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ score: result.score }));
        } catch (e) {
            res.writeHead(400);
            res.end();
        }
    });
});
server.listen(3000, () => {
    console.log('Validateur zxcvbn en écoute sur le port 3000');
});